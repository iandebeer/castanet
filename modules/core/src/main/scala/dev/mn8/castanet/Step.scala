package dev.mn8.castanet

import scala.collection.immutable.SortedMap
import scodec.bits.BitVector


case class Step(markers: Markers, count: Int = 0) :
  val inits: SortedMap[NodeId, BitVector] =
    markers.state.filter(m => m._2 > BitVector.empty.padRight(m._2.size))