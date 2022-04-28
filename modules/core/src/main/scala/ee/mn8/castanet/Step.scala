package ee.mn8.castanet

import scala.collection.immutable.SortedMap
import scodec.bits.BitVector


case class Step(markers: Markers, show: Boolean = false, count: Int = 0):
  val inits: SortedMap[NodeId, Seq[BitVector]] = markers.state.filter(m => m._2 > BitVector.empty.padRight(m._2.size))
