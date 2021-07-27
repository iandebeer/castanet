package ee.mn8.castanet

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

import cats.effect.IO
case class JwtCredentials() extends CallCredentials:
  import Constants._
  override def thisUsesUnstableApi(): Unit = {}
  override def applyRequestMetadata(
      requestInfo: CallCredentials.RequestInfo,
      appExecutor: Executor,
      applier: CallCredentials.MetadataApplier
  ): Unit =
    val headers = new Metadata()
    headers.put[String](AuthorizationMetadataKey, "test")
    applier.apply(headers)

case class KeycloakInterceptor(s: String) extends ClientInterceptor:
  override def interceptCall[Req, Res](
      methodDescriptor: MethodDescriptor[Req, Res],
      callOptions: CallOptions,
      channel: Channel
  ) =
    println("hello from the client")
    channel.newCall[Req, Res](methodDescriptor, callOptions.withCallCredentials(JwtCredentials()))

object Main extends IOApp:

  val managedChannelStream: Stream[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .intercept(KeycloakInterceptor("hi"))
      .stream[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      dispatcher     <- Stream.resource(Dispatcher[IO])
      managedChannel <- managedChannelStream
      helloStub = GreeterFs2Grpc.stub[IO](dispatcher, managedChannel, ClientOptions.default)
      // _ <- Stream.eval(runProgram(helloStub))
      _ <- Stream.eval(
        for {
          response <- helloStub.sayHello(HelloRequest("Ian de Beer"), new Metadata())
          _        <- IO(println(response.message))
        } yield ()
      )

    } yield ()
  }.compile.drain.as(ExitCode.Success)
