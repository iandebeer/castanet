object Structures:

  trait Functor[F[_]]:
    extension [A](fa: F[A])
      def map[B](f: A => B): F[B]
      def as[B](b: B): F[B] = map(_ => b)
      def void: F[Unit] = as(())

  trait Applicative[F[_]] extends Functor[F]:
    def pure[A](a: A): F[A]
    def unit: F[Unit] = pure(())
    extension[A](fa: F[A])
      def map2[B, C](fb: F[B], f: (A, B) => C): F[C]
      def tuple2[B](fb: F[B]): F[(A, B)] = map2(fb, (_, _))
      def *>[B](fb: F[B]): F[B] = map2(fb, (_, b) => b)
      def <*[B](fb: F[B]): F[A] = map2(fb, (a, _) => a)
      def map[B](f: A => B): F[B] =
        fa.map2(unit, (a, _) => f(a))

  trait Monad[F[_]] extends Applicative[F]:
    extension[A](fa: F[A])
      def flatMap[B](f: A => F[B]): F[B]
      override def map[B](f: A => B): F[B] =
        flatMap(a => pure(f(a)))
      def map2[B, C](fb: F[B], f: (A, B) => C): F[C] =
        flatMap(a => fb.map(b => f(a, b)))
    extension[A](ffa: F[F[A]])
      def flatten: F[A] = ffa.flatMap(identity)

  trait Traverse[F[_]] extends Functor[F]:
    extension [A](fa: F[A])
      def traverse[G[_], B](f: A => G[B])(using G: Applicative[G]): G[F[B]]
    extension [G[_], A](fga: F[G[A]])
      def sequence(using Applicative[G]): G[F[A]] = fga.traverse(identity)


  given Monad[List] with Traverse[List] with
    def pure[A](a: A) = List(a)
    extension[A](fa: List[A])
      def flatMap[B](f: A => List[B]) = fa.flatMap(f)
      def traverse[G[_], B](f: A => G[B])(using G: Applicative[G]): G[List[B]] =
        fa.foldRight(G.pure(Nil: List[B]))((a, acc) => f(a).map2(acc, _ :: _))

  given Monad[Option] with Traverse[Option] with
    def pure[A](a: A) = Some(a)
    extension[A](fa: Option[A])
      def flatMap[B](f: A => Option[B]) = fa.flatMap(f)
      def traverse[G[_], B](f: A => G[B])(using G: Applicative[G]): G[Option[B]] =
        fa.fold(G.pure(None: Option[B]))(a => f(a).map(Some(_)))

  opaque type Kleisli[F[_], A, B] = A => F[B]

  extension [F[_], A, B](k: Kleisli[F, A, B])
    def apply(a: A): F[B] = k(a)

  object Kleisli:
    def apply[F[_], A, B](f: A => F[B]): Kleisli[F, A, B] = f

  given [F[_], A](using F: Monad[F]): Monad[[B] =>> Kleisli[F, A, B]] with
    def pure[B](b: B) = Kleisli(_ => F.pure(b))
    extension[B](k: Kleisli[F, A, B])
      def flatMap[C](f: B => Kleisli[F, A, C]) =
        a => k(a).flatMap(b => f(b)(a))

end Structures

@main def run =
  import Structures.{*, given}

  val xs = List("1", "2", "3")
  val ys = xs.traverse(_.toIntOption)

  println(ys)
