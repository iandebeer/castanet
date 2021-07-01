package ee.mn8.castanet
import munit._
import io.circe._, io.circe.parser._
import io.circe.generic.auto._, io.circe.syntax._
import scala.io.Source
import scala.collection.immutable.ListSet

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
    val p2 = Place(2, "left", 2)
    val p3 = Place(3, "right", 1)
    val p4 = Place(4, "joint", 1)
    val p5 = Place(5, "end", 1)

    val t1 = Transition(6, "splitter", (l: LinkableElement) => println(l))
    val t2 = Transition(7, "joiner", (l: LinkableElement) => println(l))
    val t3 = Transition(8, "continuer", (l: LinkableElement) => println(l))

    val n = PetriNetBuilder().add(ListSet(p1, p2, p3, p4, p5))
    //arcs = ListSet(Arc.Weighted(from = 1l,to = 2l,weight = 1)), places = ListSet[Place](p1), transitions = ListSet[Transition](Transition(id = 2l, name = "test", fn = t)))
    val n2 = n.add(ListSet(t1, t2, t3))
    val n3 = n2
      .add(Weighted(1, 6, 1))
      .add(Weighted(6, 2, 1))
      .add(Weighted(6, 3, 1))
      .add(Weighted(2, 7, 1))
      .add(Weighted(3, 7, 1))
      .add(Weighted(7, 4, 1))
      .add(Weighted(4, 8, 1))
      .add(Weighted(8, 5, 1))
    //val x = n3.ColouredPetriNet(Map[NodeId,ListSet[LinkableElement]]())
    println("_" * 10)
    println(s"Net 3: $n3")
    println("_" * 10)
    println(s"Linkables: ${n3.build()}")
    println("_" * 10)
    PetriPrinter(graph = n3.build().graph).print()
  }
}
