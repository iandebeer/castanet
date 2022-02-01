package ee.mn8.castanet

import cats.kernel.Monoid
import scala.collection.immutable.SortedMap
import scala.collection.immutable.ListSet

import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*

import java.util.concurrent.Executor

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.IO

trait PetriElement 

trait ConcatenableProcess extends PetriElement with Monoid[PetriElement]

type NodeId = Int

case class ArcId(from: Int, to: Int):
  import scala.math.Ordered.orderingToOrdered 
  def compare(that: ArcId): Int = (this.from, this.to) compare (that.from, that.to)

enum Arc extends PetriElement : 
  val from: NodeId
  val to: NodeId
  case Timed(from: NodeId, to: NodeId, interval: Long) extends Arc
  case Weighted(from: NodeId, to: NodeId, weight: Int) extends Arc

trait  LinkableElement extends PetriElement: 
  inline def assert[T](condition:Boolean, expr:T)  = 
    if condition then expr else ()
  val id: NodeId
  val name: String
  def run():Unit

case class Place(id: NodeId, name: String, capacity: Int) extends LinkableElement:
  def run() = assert(true, println(s"Place: $name"))

case class Transition(id: NodeId, name: String, service:Service, rpc:RPC) extends LinkableElement :
    def run() = assert(true, println(s"Transition: $name"))

case class PetriElements(l: List[LinkableElement] = List[LinkableElement]()) 

type PetriGraph = SortedMap[NodeId, ListSet[LinkableElement]]