package ee.mn8.castanet


import _root_.io.grpc.*
import java.util.concurrent.Executor

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
