package ee.mn8.castanet

import Transito._
import cats.effect._
import cats.effect.kernel.Resource
import scala.io.Source
import java.io.File
import scala.io.BufferedSource
import scala.quoted.*
import scala.quoted.staging.{run, withQuotes, Compiler}
import java.lang.reflect.Modifier

import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*
import _root_.io.grpc.*
import fs2.grpc.syntax.all.*
import java.util.concurrent.Executor
import Constants.*

import scala.concurrent.ExecutionContext.Implicits.global
import fs2.grpc.client.ClientOptions
import _root_.io.grpc.ClientInterceptor

object TransitionSpec extends App:
  given Compiler = Compiler.make(getClass.getClassLoader)
  inline def debugSingle(inline expr: Any): Unit =
    ${ debugSingleImpl('expr) }

  private def debugSingleImpl(expr: Expr[Any])(using Quotes): Expr[Unit] =
    '{ println("Value of " + ${ Expr(expr.show) } + " is " + $expr) }

  private def code(using Quotes) = '{ println("foo") }

  val managedChannelStream: Stream[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .stream[IO]

  val run =
    val double = (i: Int) => i * 2
    // test("transitions petri net") {
    def test =
      transit1
      transit2
      transit3
      val x = 10
      val y = 32
      transition(x + y)
    test

    val fm = Map(("test", double))
    val z  = fm("test")(2)
    println(z)

    //val lines = try source.mkString finally source.close()

    /*    def acquireR[F[_]](paths: Seq[String])(using F: Sync[F]): Resource[F, Seq[BufferedSource]] =
      val blankResource =
        Resource.make[F, Seq[BufferedSource]](F.delay(Seq.empty[BufferedSource]))(_ => F.unit)
      (paths.map(pathResource)).foldLeft(blankResource) { case (acc, elem) =>
        for
          seq <- acc
          e   <- elem
        yield seq :+ e
      }

    def pathResource[F[_]: Sync](path: String): Resource[F, BufferedSource] =
      Resource.fromAutoCloseable(Sync[F].delay(Source.fromFile(path))) */

    //def acquire(path: String) = Resource.fromAutoCloseable(IO(Source.fromFile(path)))
    val filePaths = Seq(
      "modules/protocol/src/main/protobuf/castanet.proto",
      "modules/protocol/src/main/protobuf/tic_tac_toe.proto"
    )
    //Services.callsFromPaths(filePaths: Seq[String])
    def extractServiceDetail(filePaths: String*): Seq[Service] =
      filePaths.map(s =>
        Source
          .fromFile(s)
          .getLines
          .collect {
            case l: String if l.trim.startsWith("package") =>
              s"${l.trim.replace(";", "").split(" ")(1)}.${s.split("/").last.replace(".proto", "")}"
            case l: String if l.trim.startsWith("service") =>
              Service(name = l.trim.split(" ")(1))
            case l: String if l.trim.startsWith("rpc") =>
              val parts = l
                .replaceAll("[\\(){};]", "")
                .replace("stream", "")
                .replace("rpc", "")
                .replace("returns", "")
                .trim
                .split("\\s+")
              RPC(parts.head, parts(1), parts(2))
          }
          .foldLeft(Service())((service, l) =>
            l match {
              case p: String   => service.copy(packageName = p)
              case s1: Service => service.copy(name = s1.name)
              case r: RPC      => service.copy(rpcs = service.rpcs ++ List(r))
            }
          )
      )

    val serviceCalls = extractServiceDetail(
      "modules/protocol/src/main/protobuf/castanet.proto",
      "modules/protocol/src/main/protobuf/tic_tac_toe.proto"
    )
    println(serviceCalls)
    val serviceClasses =
      serviceCalls
        .map(s =>
          Class
            .forName(s"${s.packageName}.${s.name}Fs2Grpc")
            .getMethods()
            .collect {
              case m if Modifier.isPublic(m.getModifiers) => m.getName
            }
            .mkString(", ")
        )
        .zip(serviceCalls.map(s => s"${s.packageName}.${s.name}Fs2Grpc"))
        .map(t => (t._2, t._1))
        .mkString("\n")

    println(serviceClasses)
    //scala.quoted.staging.run(code)
    //Debug.debugSingle(x)
    //scala.quoted.staging.run(debugSingle(x))
    
    val interfaces =
      serviceCalls
        .map(s =>
          Class
            .forName(s"""${s.packageName}.${s.name}Fs2Grpc$$""")
            .getMethods()
            .collect {
              case m if Modifier.isPublic(m.getModifiers) && m.getName == "stub" =>
                println(m.getDeclaringClass().getCanonicalName)
                val obj1 = m.getDeclaringClass()
                println(s"${m.getDeclaringClass().getConstructors().mkString("\n")}")
                m.setAccessible(true)
                /*  m.invoke(
                  obj1,
                  Stream.resource(Dispatcher[IO]),
                  managedChannelStream.head,
                  ClientOptions.default
                ) */
                s"${m.getDeclaringClass}.${m.getName}"
            }
            .mkString(",")
        )
        .mkString("\n")
    println(interfaces)
