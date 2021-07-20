package ee.mn8.castanet
import munit._
import io.circe._, io.circe.parser._
import io.circe.generic.auto._, io.circe.syntax._
import scala.io.Source
import scala.collection.immutable.ListSet
import scodec.bits._
import cats.data.State

class CirceSpec extends FunSuite {
  val t = Source
    .fromFile(
      "/Users/ian/dev/castanet/modules/protocol/src/main/workflow/workflow.json"
    )
    .mkString
  val j               = parse(t).getOrElse(Json.Null)
  val k               = parse("error").getOrElse(Json.Null)
  val cursor: HCursor = j.hcursor

  // val c = cursor.downField("spec").downField("templates")
  test("extract path") {
    println(j)
    println("_" * 10)

    println(j.as[Json])
    val c1 = j.hcursor.downField("spec").downField("templates").downArray
    println("_" * 10)
    println(c1.as[Json])
    val c2 = cursor
      .downField("spec")
      .downField("templates")
      .downN(1)
      .downField("dag") //.downField("tasks").downArray
    //val x = c2.focus
    val y = decode[Dag](c2.focus.get.toString)
    println("_" * 10)

    println(c2.focus.get)
    println("_" * 10)

    println(y)
    println("_" * 10)

    //val z = decode[Workflow](j.toString)

    //println(c2.as[Json].getOrElse(Json.Null))
  }

  test("build petri net") {
    import LinkableElement._
    import Arc._
    //val p = PetriNetBuilder.ColouredPetriNet(Map[NodeId,ListSet[LinkableElement]]())
    val p1 = Place(1, "start", 1)
    val p2 = Place(2, "left", 3)
    val p3 = Place(3, "right", 1)
    val p4 = Place(4, "joint", 3)
    val p5 = Place(5, "end", 1)

    val t1 = Transition(6, "splitter", (l: LinkableElement) => println(l))
    val t2 = Transition(7, "joiner", (l: LinkableElement) => println(l))
    val t3 = Transition(8, "continuer", (l: LinkableElement) => println(l))

    val n = PetriNetBuilder().addAll(ListSet(p1, p2, p3, p4, p5))
    //arcs = ListSet(Arc.Weighted(from = 1l,to = 2l,weight = 1)), places = ListSet[Place](p1), transitions = ListSet[Transition](Transition(id = 2l, name = "test", fn = t)))
    val n2 = n.addAll(ListSet(t1, t2, t3))
    val n3 = n2
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
    println(s"Net 3: $n3")
    println("_" * 10)
    println(s"Linkables: ${n3.build()}")
    println("_" * 10)
    val pn = n3.build()
    val places = pn.elements.values.collect { case p: Place =>
      p
    }
    val dimensions = (places.size, places.maxBy(p => p.capacity).capacity)
    println(dimensions)

    val m1 = Markers(pn)
    println(s"${m1}\n${m1.toStateVector}")

    val m2 = m1.setMarker(Marker(1, bin"1"))
    println(s"${m2}\n${m2.toStateVector}")

    val m3 = m2.setMarker(Marker(2, bin"1")).setMarker(Marker(4, bin"11"))
    println(s"${m3}\n${m3.toStateVector}")

    val m4 = Markers(pn, m3.toStateVector)
    println(s"${m4}\n${m4.toStateVector} \n${m4.serialize}")

    val m5 = Markers(pn, m4.serialize)
    println(s"${m5}\n${m5.toStateVector} \n${m5.serialize}")
    PetriPrinter(fileName = "petrinet1", petriNet = pn).print(Option(m3))
    val steps: State[Step, Unit] =
      for
        p1 <- pn.step
        p2 <- pn.step
        p3 <- pn.step
      yield (
        PetriPrinter(fileName = "petrinet2", petriNet = pn).print(Option(p1)),
        PetriPrinter(fileName = "petrinet3", petriNet = pn).print(Option(p2)),
        PetriPrinter(fileName = "petrinet4", petriNet = pn).print(Option(p3))
      )
    steps.run(Step(m3, true, 1)).value

  }
}
