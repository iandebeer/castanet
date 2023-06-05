package dev.mn8.castanet
import cats.data.State
import cats.syntax.functor.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.*
import io.circe.generic.auto.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.syntax.*
import munit.*
import scodec.bits.*

import scala.collection.immutable.ListSet
import scala.io.Source
import scala.quoted.*

class PetriSpec extends FunSuite {

  test("build petri net") {
    import Arc._

    val start: Place = Place("start", 1)
    val left: Place  = Place("left", 3)
    val right: Place = Place("right", 1)
    val joint: Place = Place("joint", 3)
    val end: Place   = Place("end", 1)
    val s = Service(
      "dev.mn8.castanet",
      "HelloFs2Grpc",
      List[RPC](RPC(name = "sayHello", input = "", output = ""))
    )
    val r1 = s.rpcs.head

    val splitter: Transition  = Transition("splitter", s, r1)
    val joiner: Transition    = Transition("joiner", s, r1)
    val continuer: Transition = Transition("continuer", s, r1)

    val w1   = Weight(Colour.LIGHT_BLUE, 1)
    val w2   = Weight(Colour.LIGHT_BLUE, 1)
    val w3   = Weight(Colour.LIGHT_BLUE, 1)
    val w4   = Weight(Colour.LIGHT_BLUE, 2)
    val w5   = Weight(Colour.LIGHT_BLUE, 1)
    val w6   = Weight(Colour.LIGHT_BLUE, 1)
    val w7   = Weight(Colour.LIGHT_BLUE, 3)
    val w8   = Weight(Colour.LIGHT_BLUE, 1)
    val ptt1 = PlaceTransitionTriple(start, ListSet(w1), splitter, ListSet(w2), left)
    val ptt2 = PlaceTransitionTriple(start, ListSet(w2), splitter, ListSet(w3), right)
    val ptt3 = PlaceTransitionTriple(left, ListSet(w4), joiner, ListSet(w6), joint)
    val ptt4 = PlaceTransitionTriple(right, ListSet(w5), joiner, ListSet(w6), joint)
    val ptt5 = PlaceTransitionTriple(joint, ListSet(w7), continuer, ListSet(w8), end)

    val pn = PetriNetBuilder().add(ptt1).add(ptt2).add(ptt3).add(ptt4).add(ptt5).build()

    println("_" * 10)
    // val pn = n3.build()
    val places = pn.elements.values.collect { case p: Place =>
      p
    }
    val dimensions = (places.size, places.maxBy(p => p.capacity).capacity)
    println(dimensions)

    val m1 = Markers(pn)
    println(s"\nm1 = \n${m1}\n${m1.toStateVector} => ${m1.serialize}")
   
    println("_" * 10)

    val m2 = m1.setMarker(Marker(start.id, bin"1"))
   
    println(s"\nm2 = \n${m2}\n${m2.toStateVector} => ${m2.serialize}")
    println("_" * 10)

    val m3 = m2.setMarker(Marker(left.id, bin"1")).setMarker(Marker(joint.id, bin"11"))

    println(s"\nm3 = \n${m3}\n${m3.toStateVector} => ${m3.serialize}")
    println("_" * 10)

    val m4 = Markers(pn, m3.toStateVector)
    println(s"\nm4 = \n${m4}\n${m4.toStateVector} => ${m4.serialize}")

  
    println("_" * 10)

    val m5 = Markers(pn, m4.serialize)
  
    println(s"\nm5 = \n${m5}\n${m5.toStateVector} => ${m5.serialize}")
    println("_" * 10)

    PetriPrinter(fileName = "petrinet1", petriNet = pn).print(Option(m3))
    val steps: State[Step, Unit] =
      for
        p1 <- pn.step
        p2 <- pn.step
        p3 <- pn.step
      yield (
        println("p0 = " + pn.peek(Step(m2)).mkString(",")),
        PetriPrinter(fileName = "petrinet2", petriNet = pn).print(Option(p1)),
        println("p1 = " + pn.peek(Step(p1)).mkString(",")),
        PetriPrinter(fileName = "petrinet3", petriNet = pn).print(Option(p2)),
        println("p2 = " + pn.peek(Step(p2)).mkString(",")),
        PetriPrinter(fileName = "petrinet4", petriNet = pn).print(Option(p3)),
        println("p3 = " +pn.peek(Step(p3)).mkString(","))
      )
    steps.run(Step(m3, true, 1)).value

  }
}
