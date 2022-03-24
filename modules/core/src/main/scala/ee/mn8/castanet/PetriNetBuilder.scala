package ee.mn8.castanet

import cats._
import cats.implicits._
import ee.mn8.castanet.PetriElement._

import scala.collection.immutable.{ListSet, SortedMap}

case class PetriNetBuilder(nodes: ListSet[PetriElement] = ListSet()) extends ConcatenableProcess {

  // Set is a Semigroup but not ListSet - add monoid behaviour
  //given[T]: Semigroup[ListSet[T]] = Semigroup.instance[ListSet[T]](_ ++ _)
  implicit def listSetSemigroup[T]: Semigroup[ListSet[T]] = Semigroup.instance[ListSet[T]](_ ++ _)

  def empty: PetriNetBuilder = PetriNetBuilder()

  override def combine(n: PetriElement, a: PetriElement): ConcatenableProcess =
    PetriNetBuilder().add(n).add(a)

  def add[P <: PetriElement](p: P): PetriNetBuilder             = copy(nodes = nodes + p)
  def addAll[P <: PetriElement](p: ListSet[P]): PetriNetBuilder = copy(nodes = nodes ++ p)

  def build(): ColouredPetriNet = new ColouredPetriNet {
    override val elements: SortedMap[NodeId, LinkableElement] =
      nodes.foldRight(SortedMap[NodeId, LinkableElement]()) {
        case (n: LinkableElement, m) => m + (n.id -> n)
        case (_, m)                  => m
      }

    override val graph: PetriGraph =
      nodes.foldRight(SortedMap[NodeId, ListSet[LinkableElement]]()) {
        case (n: Arc, m) => m |+| SortedMap[NodeId, ListSet[LinkableElement]](n.from -> ListSet(elements(n.to)))
        case (_, m)      => m
      }

    override val arcs: Map[ArcId, Long] =
      nodes.collect {
        case w: Weighted => ArcId(w.from, w.to) -> w.weight.toLong
        case t: Timed    => ArcId(t.from, t.to) -> t.interval
      }.toMap
  }
}
