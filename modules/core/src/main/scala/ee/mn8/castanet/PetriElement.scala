package ee.mn8.castanet

import cats.{Monoid, Semigroup}

import scala.collection.immutable.SortedMap
import scala.collection.immutable.ListSet
import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*

import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.IO
import java.security.MessageDigest

trait PetriElement:
  def id: NodeId

//trait ConcatenableProcess extends Monoid[PetriElement]
  
type NodeId = String

case class Weight(colour: Colour, tokenCount: Int)


case class ArcId(from: NodeId, to: NodeId): 
  import scala.math.Ordered.orderingToOrdered 
  def compare(that: ArcId): Int = (this.from, this.to) compare (that.from, that.to)

enum Arc extends PetriElement : 
  val from: NodeId
  val to: NodeId
  val weight: Weight
  val id: NodeId = MessageDigest.getInstance("SHA-256")
    .digest((from + to + weight.hashCode().toHexString).getBytes("UTF-8"))
    .map("%02x".format(_)).mkString
  case Timed(from: NodeId, to: NodeId, weight: Weight, interval: Long) extends Arc
  case Weighted(from: NodeId, to: NodeId, weight: Weight) extends Arc
trait  LinkableElement extends PetriElement: 
  inline def assert[T](condition:Boolean, expr:T)  = 
    if condition then expr else ()
  val id: NodeId = MessageDigest.getInstance("SHA-256")
    .digest(name.getBytes("UTF-8"))
    .map("%02x".format(_)).mkString
  val name: String
  def run():Unit

case class Place(name: String, capacity: Int) extends LinkableElement:
  def run() = assert(true, println(s"Place: $name"))

case class Transition(name: String, service:Service, rpc:RPC) extends LinkableElement :
    def run() = assert(true, println(s"Transition: $name"))

trait PetriNet extends Monoid[PetriNet]


type PetriGraph = SortedMap[NodeId, ListSet[LinkableElement]]

