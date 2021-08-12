package ee.mn8.castanet

import munit.*
import org.dhallj.syntax.*
import org.dhallj.parser.DhallParser.parse
import org.dhallj.core.converters.JsonConverter
import org.dhallj.yaml.YamlConverter
import org.dhallj.circe.Converter
import io.circe.syntax._

class DahlSpec extends FunSuite {
  val expr1 = parse("""
let educationalBook =
      \(publisher : Text) ->
      \(title : Text) ->
        { category = "Nonfiction"
        , department = "Books"
        , publisher = publisher
        , title = title
        }

let makeOreilly = educationalBook "O'Reilly Media"

in  [ makeOreilly "Microservices for Java Developers"
    , educationalBook "Addison Wesley" "The Go Programming Language"
    , makeOreilly "Parallel and Concurrent Programming in Haskell"
    ]
    """)
  test("parse") {
    val n1 = expr1.normalize
    println(n1)
    val s = Converter(n1).asJson
    println(s)
    val y = YamlConverter.toYamlString(n1)
    println(y)

  }

}
