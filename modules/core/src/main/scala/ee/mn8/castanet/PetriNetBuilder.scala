package ee.mn8.castanet

import cats._
import cats.implicits._
import monocle.Lens
import monocle.syntax.all.*
import scala.collection.immutable.{ListSet, SortedMap}

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
      case w: Weighted => ArcId(w.from, w.to) -> w.weight.tokenCount.toLong
      case t: Timed => ArcId(t.from, t.to) -> t.interval
    }.toMap

