/*
 * Copyright 2021 Ian de Beer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ee.mn8.castanet

import fs2.{Stream,text}
import fs2.text._
import fs2.io.file._
import cats._
import cats.effect._
import cats.syntax.either._

import scala.concurrent.duration._
import java.nio.file.Paths
import io.circe.yaml._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import monocle.syntax.all._
import monocle.Lens
import scala.collection.mutable
import scala.collection.immutable.ListSet

case class Workflow(apiVersion:String, kind:String, metadata:Metadata, spec:Spec)
case class Spec(entrypoint:String, templates:List[Template])
enum Template:
  case Server(name:String, inputs:Arguments, container:Container) 
  case Service(name:String, dag: Dag) 
case class Metadata(generateName:String) 
case class Dag(tasks:List[Task])
case class Task(name:String,dependencies:Option[List[String]],template:String ,arguments: Arguments)
case class Arguments(parameters:List[Parameter])
case class Parameter(name:String,value:String)
case class Inputs(parameters:List[Parameter])
case class Container(image:String, command:List[String])

sealed trait CPN

trait ConcatenableProcess extends CPN with Monoid[CPN] 

type Id = Long

case class Place(id:Id, name:String, capacity:Int) extends CPN
case class Transition(id:Id, name: String, fn: Place => Place) extends CPN

enum Arc(from:Id, to:Id) extends CPN:
  case Timed(from:Id, to:Id,interval: Long) extends Arc(from:Id, to:Id)
  case Weighted(from:Id, to:Id, weight:Int) extends Arc(from:Id, to:Id)



case class Net(arcs:Set[Arc] = ListSet(), places:Set[Place] = ListSet(), transitions: Set[Transition] = ListSet()) extends ConcatenableProcess :
  def empty =  Net()
  override def combine(n:CPN, a:CPN):ConcatenableProcess = Net().add(n).add(a)

  val cpn = Map[Id,CPN]()
  def add[P <: CPN](p:P): Net = 
    p match 
      case p:Place => this.focus(_.places).replace(places + p) 
      case a:Arc => this.focus(_.arcs).replace(arcs + a) 
      case t:Transition => this.focus(_.transitions).replace(transitions + t) 
      case _ => println("not implemented ")
        this

object Petri extends IOApp.Simple:
  def fromYaml(s:String) =
    for
      json <- yaml.parser.parse(s)
      //x <- json.asObject
    yield json

  val converter: Stream[IO, Unit] =
    Files[IO].readAll(Paths.get("modules/protocol/src/main/workflow/workflow.yaml"), 4096)
      .through(text.utf8Decode)
      .map(fileString => fromYaml(fileString))
      .map({
        case Right(json) => json
        case Left(e:Throwable) => Json.Null.toString
      }) 
      //.map(j => extractDag(j))
      //.through(stringArrayParser)
      //.through(decoder[IO,Workflow])
      .map(w => w.toString)
      .through(text.utf8Encode)
      .through(
        Files[IO].writeAll(Paths.get("modules/protocol/src/main/workflow/workflow.txt"))
      )
  
  def run: IO[Unit] =
    val s = Stream.exec(IO.println("running...")) ++ converter ++ Stream.exec(IO(println("done!")))
    s.compile.drain
