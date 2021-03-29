import cats._
import cats.effect._
import scala.reflect.io.Path
import cats.effect.IOApp
import fs2.Stream

val s0 = Stream.empty
val s1 = Stream.emit(1)
(Stream(1,2,3) ++ Stream(4,5)).toList
def unit(companyId: Int): Unit = ()
unit(1)
1+1
val l0 : List[String] = List.empty[Nothing] 
val l1 : List[Unit] = List.empty[Nothing]
val l3 = List(())

val u : Id[Unit] = println("hello")
val l :List[Unit] = List(())
val c  = println("").getClass()

1+1