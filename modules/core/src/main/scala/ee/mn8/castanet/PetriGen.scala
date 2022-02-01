package ee.mn8.castanet

import scala.concurrent.duration.*

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.instances.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.io.file.*
import fs2.text
import fs2.text.*
import io.circe.Json
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.yaml.*
import monocle.Lens
import monocle.syntax.all.*
import java.time.Clock


case class Workflow(apiVersion: String, kind: String, metadata: Metadata, spec: Spec)
case class Spec(entrypoint: String, templates: List[Template])
enum Template:
  case Server(name: String, inputs: Arguments, container: Container)
  case Service(name: String, dag: Dag)
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

object PetriGen extends IOApp.Simple:
  def fromYaml(s: String) =
    for json <- yaml.parser.parse(s)
    //x <- json.asObject
    yield json

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

  def run: IO[Unit] =
    val s = Stream.exec(IO.println("running...")) ++ converter ++ Stream.exec(IO(println("done!")))
    s.compile.drain
