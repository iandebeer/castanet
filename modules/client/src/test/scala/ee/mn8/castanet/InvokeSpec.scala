package ee.mn8.castanet

import _root_.io.grpc.*
import _root_.io.grpc.netty.NettyChannelBuilder
import cats.effect.*
import cats.effect.std.Dispatcher
import ee.mn8.castanet.Constants.*
import fs2.*
import fs2.grpc.client.ClientOptions
import fs2.grpc.syntax.all.*
import munit.{CatsEffectSuite, FunSuite}

import java.util.concurrent.Executor
import scala.compiletime.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.deriving.*
import scala.quoted.*
import scala.quoted.staging.*

class InvokeSpec extends CatsEffectSuite {
  trait T[A]:
    val vT: A
    def mT = vT
  case class Test1[B](b: B) extends T[String]:
    val vT      = "test"
    def printVT = println(s"this is me printing $mT")
    def printB  = println(s"this is me printing $b")

  /* inline def rpcalls[T](): List[String] =
    summonFrom {
      case m:  Mirror.ProductOf[T] => literalStrings[m.MirroredElemLabels]()
      case _ => error("Not found")
    }
  inline def literalStrings[T](): List[String] =
    erasedValue[T] match
      case _: (head *: tail) => constValue[head] :: literalStrings[tail]()
      case EmptyTuple => Nil
   */
  //val tt = Expr("Test1")
  val i = Test1("testing 1")

  test("invoke") {

    val managedChannelResource: Resource[IO, ManagedChannel] =
      NettyChannelBuilder
        .forAddress("127.0.0.1", 9999)
        .usePlaintext()
        .intercept(KeycloakInterceptor("hi"))
        .resource[IO]
    /*val managedChannelStream: Stream[IO, ManagedChannel] =
      ManagedChannelBuilder
        .forAddress("127.0.0.1", 9999)
        .usePlaintext()
        .intercept(KeycloakInterceptor("hi"))
        .stream[IO]
     */
    val t = classOf[Test1[Int]]
    val c = t.getConstructors.toList.mkString(", ")
    val m = t.getDeclaredFields.toList.mkString(", ")
    val v = valueOf[Test1.type][Int](1).printB
    i.printB
    def runProgram(stub: GreeterFs2Grpc[IO, Metadata]): IO[Unit] =
      for {
        _        <- IO.println("Here we go")
        response <- stub.sayHello(HelloRequest("Ian de Beer"), new Metadata())
        message  <- IO(response.message)
        _        <- IO.println(message)
      } yield ()
    val run: IO[Unit] =
      managedChannelResource.flatMap(ch => GreeterFs2Grpc.stubResource[IO](ch)).use(runProgram)
    SyncIO(run)
  }
  test("macro test") {
    import MyMacro.*
    println(s"evaluate ${isCaseClass[Test1[Int]]}")
    println(s"evaluate ${isCaseClass[T[String]]}")
    println(showTree[Test1[String]](i))
  }
}
