package dev.mn8.castanet

import munit.FunSuite

import scala.collection.immutable.ListSet

import Arc.*

class PlaceTransitionTripleSpec extends FunSuite {
  test("create a triple") {

    val s1 = Service(
      "dev.mn8.castanet",
      "HelloFs2Grpc",
      List[RPC](RPC(name = "sayHello", input = "", output = ""))
    )
    val r1 = s1.rpcs.head

    val p1: Place      = Place("start", 2)
    val p2: Place      = Place("left", 3)
    val p3: Place      = Place("right", 3)
    val p4: Place      = Place("joint", 3)
    val p5: Place      = Place("end", 1)
    val t1: Transition = Transition("splitter", s1, r1)
    val t2: Transition = Transition("joiner", s1, r1)
    val t3: Transition = Transition("continuer", s1, r1)
    val w1             = Weight(Colour.LIGHT_BLUE, 1)
    val w2             = Weight(Colour.LIGHT_BLUE, 1)
    /*     val a3             = Weighted("6", "3", Weight(Colour.LIGHT_BLUE, 1))
    val a4             = Weighted("2", "7", Weight(Colour.LIGHT_BLUE, 2))
    val a5             = Weighted("3", "7", Weight(Colour.LIGHT_BLUE, 1))
    val a6             = Weighted("7", "4", Weight(Colour.LIGHT_BLUE, 1))
    val a7             = Weighted("4", "8", Weight(Colour.LIGHT_BLUE, 3))
    val a8             = Weighted("8", "5", Weight(Colour.LIGHT_BLUE, 1)) */

    val ptt1 = PlaceTransitionTriple(p1, ListSet(w1), t1, ListSet(w2), p2)
    /* val ptt2 = PlaceTransitionTriple(p1, ListSet(a1), t1, ListSet(a2), p2)
    val ptt3 = PlaceTransitionTriple(p1, ListSet(a1), t1, ListSet(a2), p2)
    val ptt4 = PlaceTransitionTriple(p1, ListSet(a1), t1, ListSet(a2), p2)
    val ptt5 = PlaceTransitionTriple(p1, ListSet(a1), t1, ListSet(a2), p2)
    val ptt6 = PlaceTransitionTriple(p1, ListSet(a1), t1, ListSet(a2), p2)  */
    println(s"ptt1: $ptt1")
    val cpn = PetriNetBuilder().add(ptt1).build()
    println(s"cpn = ${cpn.graph}")
  }
}
