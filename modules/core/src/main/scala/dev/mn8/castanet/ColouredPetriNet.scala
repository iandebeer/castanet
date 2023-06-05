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

package dev.mn8
package castanet

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.instances.all.*
import cats.syntax.all.*
import dev.mn8.castanet.PetriElement
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
  def peek(step: Step) = 
    val flows: Map[ArcId, Long] = arcs.filter(a => step.inits.keySet.contains(a._1.from))
    val steps: Map[ArcId, Long] = flows.filter(f => f._2 <= step.inits(f._1.from).populationCount)
    val nextFlows: Map[ArcId, Long] = for
      s <- steps
      n <- graph(s._1.to)
    yield (ArcId(s._1.to, n.id), arcs(ArcId(s._1.to, n.id)))

    // all arcs that have a wight that is less than the capacity allowed by the destination place
    val nextPlaces = nextFlows.filter(f =>
      f._2 <= elements(f._1.to)
        .asInstanceOf[Place]
        .capacity - step.markers.state(f._1.to).populationCount
    ).map(f =>
      elements(f._1.to)
    )
    val nextTransitions = nextFlows.filter(f =>
      f._2 <= elements(f._1.to)
        .asInstanceOf[Place]
        .capacity - step.markers.state(f._1.to).populationCount
    ).map(f =>
      elements(f._1.from)
    )
    nextPlaces ++ nextTransitions

end ColouredPetriNet

