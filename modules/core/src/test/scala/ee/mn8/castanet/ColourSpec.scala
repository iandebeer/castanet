package ee.mn8.castanet

import munit.*

class ColourSpec extends FunSuite {
  
  test("get colour") {
    val black = Colour.get(0)
    val white = (Colour.get(15))
    val other = Colour.get(32)
    println(s"0 = $black")
    assert(black._1 == ("BLACK"))
    println(s"15 = $white")
    assert(white._1 == ("WHITE"))
    println(s"32 = $other")
    assert(other._1 == ("COLOUR32"))

  }
}
