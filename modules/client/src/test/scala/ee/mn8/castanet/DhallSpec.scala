package ee.mn8.castanet

import munit.*
import org.dhallj.syntax.*
import org.dhallj.parser.DhallParser.parse
import org.dhallj.core.converters.JsonConverter
import org.dhallj.yaml.YamlConverter
import org.dhallj.circe.Converter

import io.circe.syntax._
import io.circe.{Decoder, Encoder} //, io.circe.generic.auto._

import scala.io.Source
import io.circe.Json
import io.circe.Decoder.Result
import io.circe.parser.decode

import ee.mn8.castanet.LinkableElement.Place
import scala.collection.immutable.ListSet

import org.dhallj.core.Expr
import org.dhallj.imports.mini.Resolver
import org.dhallj.parser.DhallParser
import java.nio.file.Path
//import org.dhallj.syntax._
//import us.oyanglul.dhall.generic._
//import LinkableElement._

class DhallSpec extends FunSuite {

  val confFile       = "modules/client/src/main/resources/petri.dhall"
  val placeFile      = "modules/client/src/main/resources/places.dhall"
  val transitionFile = "modules/client/src/main/resources/transitions.dhall"

  test("read from file") {
    val conf = Source.fromFile(confFile).getLines.mkString("\n")
    //val places      = Source.fromFile(placeFile).getLines.mkString("\n")
    //val transitions = Source.fromFile(transitionFile).getLines.mkString("\n")

    val expr = parse(conf)
    //val placeList      = parse(places)
    //val transitionList = parse(transitions)

    //val normP = Expr.makeApplication(placeList.normalize, expr)
    //val normT = Expr.makeApplication(transitionList.normalize, expr)
    val normE = expr.normalize
    //val normP = placeList.normalize
    //val normT = transitionList.normalize

    //println(s"\n\n${expr}\n\n${expr.normalize}")

    println(s"\n\n${normE}\n$normE")

    //println(s"\n\n${placeList.normalize}\n${transitionList.normalize}")

    /* val listP = normP.as[List[Place]].getOrElse(List[Place]())
    val listT = normT.as[List[Transition]].getOrElse(List[Transition]())
    val list  = ListSet[LinkableElement]() ++ listP ++ listT
    println(s"\n\nlist: ${list}")

    println(PetriNetBuilder().addAll(list).build().elements) */

  }
}
