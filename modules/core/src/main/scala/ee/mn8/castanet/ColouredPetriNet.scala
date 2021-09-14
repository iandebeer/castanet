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

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.instances.all.*
import cats.syntax.all.*
import ee.mn8.castanet.PetriElement
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.yaml.*
import monocle.Lens
import monocle.syntax.all.*
import scodec.bits.{Bases, BitVector, ByteOrdering}

import java.nio.file.Paths
import scala.collection.immutable.{ListSet, SortedMap}
import scala.collection.mutable
import scala.concurrent.duration.*

case class Marker(id: NodeId, bits: BitVector) extends Monoid[Marker] :
  val asMap = Map(id -> bits)

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
        .collect { case p: Place =>
          (p.id, BitVector.fill(p.capacity)(false))
        }
        .to(collection.immutable.SortedMap)
    )

  def apply(cpn: ColouredPetriNet, stateVector: BitVector): Markers =
    Markers(
      cpn,
      cpn.elements.values
        .collect { case p: Place =>
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

end Markers

case class Markers(cpn: ColouredPetriNet, state: SortedMap[NodeId, BitVector]):
  //import LinkableElement._

  val places = cpn.elements.values.collect { case p: Place => (p.id, p) }.toMap

  def setMarker(m: Marker) =
    if state.keySet.contains(m.id) then Markers(cpn, state ++ m.asMap)
    else this

  def toStateVector = state.foldLeft(BitVector.empty)((b, kv) =>
    kv._2 match
      case BitVector.empty => b ++ BitVector.fill(places(kv._1).capacity.toLong)(false)
      case v: BitVector => b ++ v
  )

  def serialize = toStateVector.toBase64
end Markers

case class Step(markers: Markers, show: Boolean = false, count: Int = 0):
  val inits: SortedMap[NodeId, BitVector] = markers.state.filter(m => m._2 > BitVector.empty.padRight(m._2.size))

trait ColouredPetriNet:
  //import LinkableElement._

  import cats.data.State

  val elements: SortedMap[NodeId, LinkableElement]
  val graph: PetriGraph
  val arcs: Map[ArcId, Long]

  /**
   * Providing a state monad for traversing the Petri Net
   *
   */
  def step: State[Step, Markers] = State(step =>
    // all arcs that come from places with tokens
    val flows: Map[ArcId, Long] = arcs.filter(a => step.inits.keySet.contains(a._1.from))

    // all arcs that have a smaller guards than the number of markers in the place - i.e. it can step 
    val steps: Map[ArcId, Long] = flows.filter(f => f._2 <= step.inits(f._1.from).populationCount)

    // all arcs from allowable transitions (steps) and their weights 
    val nextFlows: Map[ArcId, Long] = for
      s <- steps
      n <- graph(s._1.to)
    yield (ArcId(s._1.to, n.id), arcs(ArcId(s._1.to, n.id)))

    // all arcs that have a wight that is less than the capacity allowed by the destination place
    val nextSteps = nextFlows.filter(f =>
      f._2 <= elements(f._1.to)
        .asInstanceOf[Place]
        .capacity - step.markers.state(f._1.to).populationCount
    )

    // remove markers from the origin place of allowed steps 
    val m1 = steps.foldLeft(step.markers)((m, s) => m.setMarker(Marker(s._1.from, step.markers.state(s._1.from).shiftLeft(s._2))))

    // add markers to the destination place (as per the weight from the transition)
    val m2 = nextFlows.foldLeft(m1)((m, s) => m.setMarker(Marker(s._1.to, step.markers.state(s._1.to).patch(step.markers.state(s._1.to).populationCount, BitVector.fill(s._2)(true)))))

    // this side effect must be moved to the IO monad 
    if step.show then
      PetriPrinter(fileName = s"step${step.count}", petriNet = this).print(markers = Option(step.markers), steps = Option(steps ++ nextSteps))
    else ()

      // update the state and return the markers resulting from the step (reduced origin and increased destination steps)
      (Step(m2, step.show, step.count + 1), m2)
  )
end ColouredPetriNet

case class PetriNetBuilder(nodes: ListSet[PetriElement] = ListSet()) extends ConcatenableProcess :

  import Arc.*

  // Set is a Semigroup but not ListSet - add monoid behaviour
  given[T]: Semigroup[ListSet[T]] = Semigroup.instance[ListSet[T]](_ ++ _)

  def empty = PetriNetBuilder()

  override def combine(n: PetriElement, a: PetriElement): ConcatenableProcess =
    PetriNetBuilder().add(n).add(a)

  def add[P <: PetriElement](p: P): PetriNetBuilder =
    this.focus(_.nodes).replace(nodes + p)

  def addAll[P <: PetriElement](p: ListSet[P]): PetriNetBuilder =
    this.focus(_.nodes).replace(nodes ++ p)

  def build() = new ColouredPetriNet :
    override val elements = nodes.foldRight(SortedMap[NodeId, LinkableElement]())((n, m) =>
      n match
        case p: LinkableElement => m + (p.id -> p)
        case _ => m
    )
    override val graph = nodes.foldRight(SortedMap[NodeId, ListSet[LinkableElement]]())((n, m) =>
      n match
        case t: Arc =>
          m |+| SortedMap[NodeId, ListSet[LinkableElement]](t.from -> ListSet(elements(t.to)))
        case _ => m
    )

    override val arcs = nodes.collect {
      case w: Weighted => ArcId(w.from, w.to) -> w.weight.toLong
      case t: Timed => ArcId(t.from, t.to) -> t.interval
    }.toMap

