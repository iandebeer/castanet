package ee.mn8.castanet

import cats.kernel.Monoid

import scala.collection.immutable.{ListSet, SortedMap}

trait PetriElement

object PetriElement {
  type NodeId = Int

  trait ConcatenableProcess extends PetriElement with Monoid[PetriElement]

  abstract class Arc extends PetriElement {
    val from: NodeId
    val to: NodeId
  }

  case class Timed(from: NodeId, to: NodeId, interval: Long) extends Arc
  case class Weighted(from: NodeId, to: NodeId, weight: Int) extends Arc

  case class ArcId(from: Int, to: Int) {
    import scala.math.Ordered.orderingToOrdered
    def compare(that: ArcId): Int = (this.from, this.to) compare (that.from, that.to)
  }

  trait LinkableElement extends PetriElement {
    val id: NodeId
    val name: String
    /*inline*/
    def assert[T](condition: Boolean, expr: T): Any = if (condition) expr else ()
    def run(): Unit
  }

  case class Place(id: NodeId, name: String, capacity: Int) extends LinkableElement {
    def run(): Unit = assert(condition = true, println(s"Place: $name"))
  }

  case class Transition(id: NodeId, name: String, service: Service, rpc: RPC)
      extends LinkableElement {
    def run(): Unit = assert(condition = true, println(s"Transition: $name"))
  }

  type PetriGraph = SortedMap[NodeId, ListSet[LinkableElement]]

}
