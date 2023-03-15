package dev.mn8.castanet

import cats.Semigroup

import java.security.MessageDigest
import scala.collection.immutable.ListSet
import Arc._

//trait ConcatenableProcess extends PetriElements  
case class PlaceTransitionTriple(start: Place, inWeights: ListSet[Weight], transition: Transition, outWeights: ListSet[Weight], end:Place) extends Semigroup[PlaceTransitionTriple]  :
  val consumers: ListSet[Arc] = inWeights.map(c => Weighted(start.id,  transition.id, c)) 
  val producers = outWeights.map(p => Weighted(transition.id,  end.id, p))

  val id: NodeId = MessageDigest.getInstance("SHA-256")
    .digest((start.id
      + consumers.foldLeft(List[String]()){(x, y) => x :+ y.id}.mkString
      + transition.id
      + consumers.foldLeft(List[String]()){(x, y) => x :+ y.id}.mkString
      + end.id).getBytes("UTF-8"))
    .map("%02x".format(_)).mkString
 
  //def combineWith(other:PlaceTransitionTriple) = combine(this, other)
  val graph: ListSet[PetriElement] = ListSet(start, end, transition) ++  consumers ++ producers

  def combine(base: PlaceTransitionTriple, other:PlaceTransitionTriple ) = 
    if base.start.id == other.start.id && base.end.id == other.end.id  && base.transition.id == other.transition.id then
      base.copy(start, inWeights ++ other.inWeights, transition, outWeights ++ other.outWeights, end) 
    else 
      base



