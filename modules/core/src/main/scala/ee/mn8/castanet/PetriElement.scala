package ee.mn8.castanet

import cats.kernel.Monoid
import scala.collection.immutable.SortedMap
import scala.collection.immutable.ListSet
import io.grpc.ManagedChannelBuilder

import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*
import _root_.io.grpc.*
import fs2.grpc.syntax.all.*
import java.util.concurrent.Executor
import Constants.*

import scala.concurrent.ExecutionContext.Implicits.global
import fs2.grpc.client.ClientOptions
import _root_.io.grpc.ClientInterceptor

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
  
  val managedChannelStream: Stream[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .intercept(KeycloakInterceptor("hi"))
      .stream[IO]
  def run() = assert(true, println(s"Transition: $name"))

case class PetriElements(l: List[LinkableElement] = List[LinkableElement]()) 

type PetriGraph = SortedMap[NodeId, ListSet[LinkableElement]]