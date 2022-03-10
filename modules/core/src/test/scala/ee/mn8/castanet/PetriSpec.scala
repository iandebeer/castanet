package ee.mn8.castanet
import cats.data.State
import ee.mn8.castanet.PetriElement._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import munit._
import scodec.bits._

import scala.collection.immutable.ListSet

class PetriSpec extends FunSuite {

  test("build petri net") {
    val place1Json = """{"id":1,"name":"start","capacity":1}"""

    val place1: Place = decode[Place](place1Json).getOrElse(Place(999, "Error", 0))
    val place1String  = place1.asJson.noSpaces

    println(s"\nPlace:\n$place1String\n")

    val place2: Place = Place(2, "left", 3)
    val place3: Place = Place(3, "right", 1)
    val place4: Place = Place(4, "joint", 3)
    val place5: Place = Place(5, "end", 1)

    // val rpc =

    /*    val jt1 = """{"id":6,"name":"splitter","capacity":1}""""""
    val l   = (l: String) => println(l)*/

    val service1 = Service(
      "ee.mn8.castanet",
      "HelloFs2Grpc",
      List[RPC](RPC(name = "sayHello", input = "", output = ""))
    )
    val rpc1 = service1.rpcs.head
    // def func(serviceName: String, rpcName: String): Function1[String, Unit] = ???
    // '{(l: String) => println(l)}

    val transition1: Transition = Transition(6, "splitter", service1, rpc1)
    /*
    val tt1       = t1.asJson.noSpaces
     */

    val transition2: Transition = Transition(7, "joiner", service1, rpc1)
    val transition3: Transition = Transition(8, "continuer", service1, rpc1)

    val builderWithPlaces =
      PetriNetBuilder().addAll(ListSet(place1, place2, place3, place4, place5))
    val placeListJson = List(place1, place2, place3, place4, place5).asJson.spaces2
    println(s"\n\nJSON LIST:\n $placeListJson")
    val placeList = decode[List[Place]](placeListJson)
    println(s"\n\nJSON decoded: $placeList")

    val transitionListJsonStr = List(transition1, transition2, transition3).asJson.spaces2
    println(s"\n\nJSON LIST:\n $transitionListJsonStr")
    val transitionList = decode[List[Transition]](transitionListJsonStr)
    println(s"\n\nJSON decoded: $transitionList")

    //arcs = ListSet(Arc.Weighted(from = 1l,to = 2l,weight = 1)), places = ListSet[Place](p1), transitions = ListSet[Transition](Transition(id = 2l, name = "test", fn = t)))
    val builderWithPlacesAndTransitions =
      builderWithPlaces.addAll(ListSet(transition1, transition2, transition3))
    val builderWithWeights =
      builderWithPlacesAndTransitions
        .add(Weighted(1, 6, 1))
        .add(Weighted(6, 2, 1))
        .add(Weighted(6, 3, 1))
        .add(Weighted(2, 7, 2))
        .add(Weighted(3, 7, 1))
        .add(Weighted(7, 4, 1))
        .add(Weighted(4, 8, 3))
        .add(Weighted(8, 5, 1))
    //val x = n3.ColouredPetriNet(Map[NodeId,ListSet[LinkableElement]]())
    println("_" * 10)
    println(s"Net 3: $builderWithWeights")
    println("_" * 10)
    println(s"Linkables: ${builderWithWeights.build()}")
    println("_" * 10)
    val colouredPetriNet = builderWithWeights.build()
    val places           = colouredPetriNet.elements.values.collect { case p: Place => p }
    val dimensions       = (places.size, places.maxBy(p => p.capacity).capacity)

    println(dimensions)

    val m1 = Markers(colouredPetriNet)
    println(s"${m1}\n${m1.toStateVector}")

    val m2 = m1.setMarker(Marker(1, bin"1"))
    println(s"${m2}\n${m2.toStateVector}")

    val m3 = m2.setMarker(Marker(2, bin"1")).setMarker(Marker(4, bin"11"))
    println(s"${m3}\n${m3.toStateVector}")

    val m4 = Markers(colouredPetriNet, m3.toStateVector)
    println(s"${m4}\n${m4.toStateVector} \n${m4.serialize}")

    val m5 = Markers(colouredPetriNet, m4.serialize)
    println(s"${m5}\n${m5.toStateVector} \n${m5.serialize}")
    PetriPrinter(fileName = "petrinet1", petriNet = colouredPetriNet).print(Option(m3))
    val steps: State[Step, Unit] =
      for {
        p1 <- colouredPetriNet.step
        p2 <- colouredPetriNet.step
        p3 <- colouredPetriNet.step
      } yield (
        PetriPrinter(fileName = "petrinet2", petriNet = colouredPetriNet).print(Option(p1)),
        PetriPrinter(fileName = "petrinet3", petriNet = colouredPetriNet).print(Option(p2)),
        PetriPrinter(fileName = "petrinet4", petriNet = colouredPetriNet).print(Option(p3))
      )
    steps.run(Step(m3, true, 1)).value

  }
}
