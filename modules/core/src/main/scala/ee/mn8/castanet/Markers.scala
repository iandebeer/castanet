package ee.mn8.castanet

import ee.mn8.castanet.{ColouredPetriNet, Marker}
import ee.mn8.castanet.PetriElement._
import scodec.bits.{Bases, BitVector}

import scala.collection.immutable.SortedMap

case class Markers(cpn: ColouredPetriNet, state: SortedMap[NodeId, BitVector]) {
  //import LinkableElement._

  val places: Map[NodeId, Place] = cpn.elements.values.collect { case p: Place => (p.id, p) }.toMap

  def setMarker(m: Marker): Markers =
    if (state.keySet.contains(m.id)) Markers(cpn, state ++ m.asMap)
    else this

  def toStateVector: BitVector = state.foldLeft(BitVector.empty)((b, kv) =>
    kv._2 match {
      case BitVector.empty => b ++ BitVector.fill(places(kv._1).capacity.toLong)(false)
      case v: BitVector    => b ++ v
    }
  )

  def serialize: String = toStateVector.toBase64
} // end Markers

object Markers {
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
        .foldLeft((SortedMap[NodeId, BitVector](), stateVector)) { (ms, kv) =>
          val t = ms._2.splitAt(kv._2)
          (ms._1 ++ Map(kv._1 -> t._1), t._2)
        }
        ._1
    )

  def apply(cpn: ColouredPetriNet, markers: String): Markers =
    Markers(cpn, BitVector.fromValidBase64(markers, Bases.Alphabets.Base64))
} //end Markers

