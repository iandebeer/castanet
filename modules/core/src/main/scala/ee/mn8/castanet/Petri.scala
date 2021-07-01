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

package ee.mn8
package castanet

import fs2.{Stream,text}
import fs2.text._
import fs2.io.file._
import cats.*, cats.syntax.all.*, cats.implicits.*
import cats.effect.*
import cats.instances.all.*

import scala.concurrent.duration._
import java.nio.file.Paths
import io.circe.yaml._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import monocle.syntax.all._
import monocle.Lens
import scala.collection.mutable
import scala.collection.immutable.ListSet
import ee.mn8.castanet.PetriElement
import org.yaml.snakeyaml.nodes.NodeId
//import ee.mn8.castanet.ColouredPetriNet

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

sealed trait PetriElement
sealed trait ConcatenableProcess extends PetriElement with Monoid[PetriElement] 

type NodeId = Int

enum Arc extends PetriElement:
  val from:NodeId
  val to:NodeId
  case  Timed(from:NodeId, to:NodeId,interval: Long) extends Arc
  case  Weighted(from:NodeId, to:NodeId, weight:Int) extends Arc

enum LinkableElement extends PetriElement :
  val id:NodeId
  val name: String
  case  Place(id:NodeId, name:String, capacity:Int) extends LinkableElement
  case  Transition(id:NodeId, name: String, fn: LinkableElement => Unit) extends LinkableElement

type PetriGraph = Map[NodeId,ListSet[LinkableElement]] 


case class PetriNetBuilder(nodes:ListSet[PetriElement] = ListSet()) extends ConcatenableProcess :
  case class ColouredPetriNet(val graph: PetriGraph) 
// Set is a Semigroup but not ListSet
  given [T]:Semigroup[ListSet[T]] = Semigroup.instance[ListSet[T]](_ ++ _)

  def empty = PetriNetBuilder()  
  override def combine(n:PetriElement, a:PetriElement):ConcatenableProcess = PetriNetBuilder().add(n).add(a)

  def add[P <: PetriElement](p:P): PetriNetBuilder = 
    this.focus(_.nodes).replace(nodes + p)
  def add[P <: PetriElement](p:ListSet[P]): PetriNetBuilder = 
    this.focus(_.nodes).replace(nodes ++ p)
  def build(): ColouredPetriNet = 
    val linkables = nodes.foldRight(Map[NodeId,LinkableElement]())((n,m) => n match 
      case p:LinkableElement => m + (p.id -> p) 
      case _ => m
    )
    val linked = nodes.foldRight(Map[NodeId,ListSet[LinkableElement]]())((n,m) => n match 
      case t:Arc => m |+| Map[NodeId,ListSet[LinkableElement]](t.from -> ListSet(linkables(t.to)))
      case _ => m
    )
    new ColouredPetriNet(linked)
  
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
