package ee.mn8.castanet

import cats._
import cats.effect._
import cats.implicits._
import cats.instances.all._
import cats.syntax.all._
import fs2.io.file._
import fs2.{text, Stream}
import fs2.text._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.yaml._
import java.time.Clock
import scala.concurrent.duration._

object PetriGen extends IOApp.Simple {
  def fromYaml(s: String): Either[ParsingFailure, Json] =
    yaml.parser.parse(s)


  val converter: Stream[IO, Unit] =
    Files[IO]
      .readAll(Path("modules/protocol/src/main/workflow/workflow.yaml"))
      .through(text.utf8.decode)
      .map(fileString => fromYaml(fileString))
      .map({
        case Right(json)        => json
        case Left(e: Throwable) => Json.Null.toString
      })
      //.map(j => extractDag(j))
      //.through(stringArrayParser)
      //.through(decoder[IO,Workflow])
      .map(w => w.toString)
      .through(text.utf8.encode)
      .through(
        Files[IO].writeAll(Path("modules/protocol/src/main/workflow/workflow.txt"))
      )

  def run: IO[Unit] = {
    val s = Stream.exec(IO.println("running...")) ++ converter ++ Stream.exec(IO(println("done!")))
    s.compile.drain
  }

  case class Workflow(apiVersion: String, kind: String, metadata: Metadata, spec: Spec)
  case class Spec(entrypoint: String, templates: List[Template])
  sealed trait Template
  case class Server(name: String, inputs: Arguments, container: Container) extends Template
  case class Service(name: String, dag: Dag)                               extends Template

  case class Metadata(generateName: String)
  case class Dag(tasks: List[Task])
  case class Task(
      name: String,
      dependencies: Option[List[String]],
      template: String,
      arguments: Arguments
  )
  case class Arguments(parameters: List[Parameter])
  case class Parameter(name: String, value: String)
  case class Inputs(parameters: List[Parameter])
  case class Container(image: String, command: List[String])
}
