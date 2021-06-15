package ee.mn8.castanet

import fs2._
import cats.effect._
import io.grpc._

import fs2.grpc.syntax.all._
//import _root_.io.grpc.BindableService
import fs2.grpc.server.ServerOptions
import _root_.io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import Constants._
case class AuthInterceptor(msg: String = "hello") extends ServerInterceptor:
  override def interceptCall[Req,Res] (
      call: ServerCall[Req, Res],
      requestHeaders: Metadata,
      next: ServerCallHandler[Req, Res]) = 
        println(s"$msg: ${requestHeaders.get(Constants.AuthorizationMetadataKey)}")
        next.startCall(call,requestHeaders)

class GreeterImpl extends GreeterFs2Grpc[IO, Metadata] :
  override def sayHello(request: HelloRequest,
                        clientHeaders: Metadata): IO[HelloReply] =
    IO(HelloReply("Request name is: " + request.name))


  override def sayHelloStream(request: Stream[IO, HelloRequest],clientHeaders: Metadata): Stream[IO, HelloReply] = 
    request.evalMap(req => sayHello(req, clientHeaders))

object Main extends IOApp.Simple:
  //extension(i:ServerInterceptor) def interceptWith: ServerServiceDefinition
  val helloService: Resource[IO, ServerServiceDefinition] =
    GreeterFs2Grpc.bindServiceResource[IO](new GreeterImpl, ServerOptions.default )


  def run: IO[Unit] =
    val myFavouriteSync: Async[IO] = Async[IO]
    val startup: IO[Any] = helloService.use{ service =>
      ServerBuilder
        .forPort(9999)
        .addService(service)
        .intercept(AuthInterceptor("hi there: "))
        .resource[IO](myFavouriteSync)
        .evalMap(server => IO(server.start()))
        .useForever
    }
    startup >> IO.unit