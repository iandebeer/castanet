package ee.mn8.castanet

import cats.Monoid
import ee.mn8.castanet.PetriElement.NodeId
import scodec.bits.BitVector

case class Marker(id: NodeId, bits: BitVector) extends Monoid[Marker] {
  val asMap = Map(id -> bits)

  override def empty: Marker = Marker(id, BitVector.empty)

  def add(m: Marker): Marker = combine(this, m)

  override def combine(m1: Marker, m2: Marker): Marker =
    if (m1.id == m2.id) {
      Marker(m1.id, (m1.bits | m2.bits))
    } else {
      m1
    }
}
