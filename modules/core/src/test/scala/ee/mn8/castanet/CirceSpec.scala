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
  val j = parse(t).getOrElse(Json.Null)
  val k = parse("error").getOrElse(Json.Null)
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
    val p1 = Place(1l,"start", 1)
    val t: Place => Place = (p:Place) => p
    val n = Net(arcs = ListSet(Arc.Weighted(from = 1l,to = 2l,weight = 1)), places = ListSet[Place](p1), transitions = ListSet[Transition](Transition(id = 2l, name = "test", fn = t)))
    val n2 = n.add(Place(2l,"test",1))
    val n3 = n.add(Arc.Weighted(1l,2l,1)).add(Arc.Weighted(2l,1l,1))
    println("_" * 10)
    println(s"Net 1: $n")
    println(s"Net 2: $n2")
    println(s"Net 3: $n3")

    println("_" * 10)

  }
}
