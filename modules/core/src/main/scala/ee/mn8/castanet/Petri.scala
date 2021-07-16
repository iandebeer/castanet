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

import fs2.{Stream, text}
import fs2.text.*
import fs2.io.file.*
import cats.*
import cats.syntax.all.*
import cats.implicits.*
import cats.effect.*
import cats.instances.all.*

import scala.concurrent.duration.*
import java.nio.file.Paths
import io.circe.yaml.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import monocle.syntax.all.*
import monocle.Lens

import scala.collection.mutable
import scala.collection.immutable.ListSet
import ee.mn8.castanet.PetriElement
import scodec.bits.{Bases, BitVector}
import scala.collection.immutable.SortedMap
import scodec.bits.ByteOrdering

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

sealed trait PetriElement
sealed trait ConcatenableProcess extends PetriElement with Monoid[PetriElement]

type NodeId = Int

enum Arc extends PetriElement:
  val from: NodeId
  val to: NodeId
  case Timed(from: NodeId, to: NodeId, interval: Long) extends Arc
  case Weighted(from: NodeId, to: NodeId, weight: Int) extends Arc

enum LinkableElement extends PetriElement:
  val id: NodeId
  val name: String
  case Place(id: NodeId, name: String, capacity: Int) extends LinkableElement
  case Transition(id: NodeId, name: String, fn: LinkableElement => Unit) extends LinkableElement

type PetriGraph = SortedMap[NodeId, ListSet[LinkableElement]]

case class Marker(id: NodeId, bits: BitVector) extends Monoid[Marker]:
  val asMap          = Map(id -> bits)
  override def empty = Marker(id, BitVector.empty)
  def add(m: Marker) = combine(this, m)
  override def combine(m1: Marker, m2: Marker): Marker =
    if m1.id == m2.id then Marker(m1.id, (m1.bits | m2.bits))
    else m1

object Markers:
  def apply(cpn: ColouredPetriNet): Markers =
    Markers(
      cpn,
      cpn.elements.values
        .collect { case p: LinkableElement.Place =>
          (p.id, BitVector.fill(p.capacity)(false))
        }
        .to(collection.immutable.SortedMap)
    )

  def apply(cpn: ColouredPetriNet, stateVector: BitVector): Markers =
    Markers(
      cpn,
      cpn.elements.values
        .collect { case p: LinkableElement.Place =>
          (p.id, p.capacity)
        }
        .foldLeft((SortedMap[NodeId, BitVector](), stateVector))((ms, kv) =>
          val t = ms._2.splitAt(kv._2)
          (ms._1 ++ Map(kv._1 -> t._1), t._2)
        )
        ._1
    )

  def apply(cpn: ColouredPetriNet, markers: String): Markers =
    Markers(cpn, BitVector.fromValidBase64(markers, Bases.Alphabets.Base64))

case class Markers(cpn: ColouredPetriNet, state: SortedMap[NodeId, BitVector]):
  import LinkableElement._

  val places = cpn.elements.values.collect { case p: Place => (p.id, p) }.toMap
  def setMarker(m: Marker) =
    if state.keySet.contains(m.id) then Markers(cpn, state ++ m.asMap)
    else this
  def toStateVector = state.foldLeft(BitVector.empty)((b, kv) =>
    kv._2 match
      case BitVector.empty => b ++ BitVector.fill(places(kv._1).capacity.toLong)(false)
      case v: BitVector    => b ++ v
  )
  def serialize = toStateVector.toBase64

case class ArcId(from: Int, to: Int)

trait ColouredPetriNet:
  import LinkableElement._
  val elements: Map[NodeId, LinkableElement]
  val graph: PetriGraph
  val arcs: Map[ArcId, Long]
  def step(markers: Markers, show: Boolean = false, stepCount:Int = 0) =
    val inits: SortedMap[NodeId, BitVector] =
      markers.state.filter(m => m._2 > BitVector.empty.padRight(m._2.size))
    // println(s"inits: $inits")
    val flows: Map[ArcId, Long] = arcs.filter(a => inits.keySet.contains(a._1.from))
    println(s"flows: $flows")

    //SortedMap[NodeId,ListSet[LinkableElement]]
    val steps: Map[ArcId, Long] = flows.filter(f => f._2 <= inits(f._1.from).populationCount)
    println(s"steps: $steps")
    val nextFlows: Map[ArcId, Long] = for
      s <- steps
      n <- graph(s._1.to)
    yield (ArcId(s._1.to, n.id), arcs(ArcId(s._1.to, n.id)))
    println(s"markers: ${markers.state}\nnext flows: $nextFlows")
    val nextSteps = nextFlows.filter(f =>
      f._2 <= elements(f._1.to)
        .asInstanceOf[Place]
        .capacity - markers.state(f._1.to).populationCount
    )

    //val nextSteps = nextFlows.filter(f => f._2.toInt <= elements(f._1.split("_").last.toInt).asInstanceOf[Place].capacity)
    if show then
      PetriPrinter(fileName = s"step$stepCount", petriNet = this)
        .print(markers = Option(markers), steps = Option(steps ++ nextSteps))
    //PetriPrinter(fileName = "next",petriNet = this).print(markers = Option(markers),steps = Option(nextSteps))
     
    val m1 = steps.foldLeft(markers)((m,s) => m.setMarker(Marker(s._1.from,markers.state(s._1.from).shiftLeft(s._2))))
    println(s"markers after step1:$m1 ")

    val m2 = nextFlows.foldLeft(m1)((m,s) => m.setMarker(Marker(s._1.to,markers.state(s._1.to).patch(markers.state(s._1.to).populationCount, BitVector.fill(s._2)(true)))))

    println(s"markers after step2:$m2 ")
    m2

  def run(markers: Markers, show: Boolean = false): List[Int] = ???

case class PetriNetBuilder(nodes: ListSet[PetriElement] = ListSet()) extends ConcatenableProcess:
  import Arc._
// Set is a Semigroup but not ListSet - add monoid behaviour
  given [T]: Semigroup[ListSet[T]] = Semigroup.instance[ListSet[T]](_ ++ _)

  def empty = PetriNetBuilder()
  override def combine(n: PetriElement, a: PetriElement): ConcatenableProcess =
    PetriNetBuilder().add(n).add(a)

  def add[P <: PetriElement](p: P): PetriNetBuilder =
    this.focus(_.nodes).replace(nodes + p)
  def addAll[P <: PetriElement](p: ListSet[P]): PetriNetBuilder =
    this.focus(_.nodes).replace(nodes ++ p)
  def build() = new ColouredPetriNet:
    override val elements = nodes.foldRight(Map[NodeId, LinkableElement]())((n, m) =>
      n match
        case p: LinkableElement => m + (p.id -> p)
        case _                  => m
    )
    override val graph = nodes.foldRight(SortedMap[NodeId, ListSet[LinkableElement]]())((n, m) =>
      n match
        case t: Arc =>
          m |+| SortedMap[NodeId, ListSet[LinkableElement]](t.from -> ListSet(elements(t.to)))
        case _ => m
    )

    override val arcs = nodes.collect {
      case w: Weighted => ArcId(w.from, w.to) -> w.weight.toLong
      case t: Timed    => ArcId(t.from, t.to) -> t.interval
    }.toMap

object Petri extends IOApp.Simple:
  def fromYaml(s: String) =
    for json <- yaml.parser.parse(s)
    //x <- json.asObject
    yield json

  val converter: Stream[IO, Unit] =
    Files[IO]
      .readAll(Paths.get("modules/protocol/src/main/workflow/workflow.yaml"), 4096)
      .through(text.utf8Decode)
      .map(fileString => fromYaml(fileString))
      .map({
        case Right(json)        => json
        case Left(e: Throwable) => Json.Null.toString
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
