package ee.mn8.castanet

import ee.mn8.castanet.PetriElement._

import java.io.{File, PrintWriter}

case class PetriPrinter(
    path: String = "./",
    fileName: String = "petrinet",
    petriNet: ColouredPetriNet
) {

  import PetriPrinter.LinkableElementElementPrinter

  /** Creates an output dot file and uses that to create graphviz png output using following command
    * dot -Tpng <filePrefix>.dot -o <filePrefix>.png If you want to change the certain format,
    * change below.
    */
  def print(markers: Option[Markers] = None, steps: Option[Map[ArcId, Long]] = None): Unit = {
    val builder = new StringBuilder("digraph G {\n")
      .append(printElements(markers))
      .append("\n")
      .append(arcString(steps))
      .append("}")
    writeTextToFile(fileName + ".dot", builder.toString)
    executeCommand(dotCommand)
  }

  private def dotCommand: String = {
    new StringBuilder()
      .append("dot -Tpng:cairo:gd ")
      .append(path + fileName)
      .append(".dot ") // output type
      .append("-o ")   // input dot file
      .append(path + fileName)
      .append(".png") // output image
      .toString
  }

  private def printElements(markersOpt: Option[Markers]): String = {
    petriNet.elements.foldLeft("") { case (acc, (_, elem)) =>
      acc + elem.printString(markersOpt)
    }
  }

  private def arcString(steps: Option[Map[ArcId, Long]]): String = {
    petriNet.graph.foldLeft("") { case (acc, (nodeId, linkableSet)) =>
      val linkableSetString: String =
        linkableSet.toIndexedSeq.reverse.map { l =>
          val stepsString = if (steps.contains(ArcId(nodeId, l.id))) { ",color=red,penwidth=3.0" }
          else { "" }
          s"""$nodeId -> ${l.id} [label="${petriNet.arcs(ArcId(nodeId, l.id))}" $stepsString] \n"""
        }.mkString

      acc + linkableSetString
    }
  }

  def executeCommand(command: String): Unit =
    Runtime.getRuntime.exec(command)

  def writeTextToFile(fileName: String, text: String): Unit = {
    val writer = new PrintWriter(new File(path + fileName))
    writer.write(text)
    writer.close()
  }
}

object PetriPrinter {

  implicit class LinkableElementElementPrinter(e: LinkableElement) {

    def printString(markersOpt: Option[Markers]): String = {
      e match {
        case Place(id, name, capacity) =>
          val markerString = markersOpt match {
            case Some(m) =>
              val up   = m.state(id).populationCount
              val down = capacity - up

              ("•" * up.toInt) + ("_" * down.toInt)

            case None => "°" * capacity
          }

          s"""$id [label="$name\\n$markerString\\n" shape=circle]\n"""

        case Transition(id, name, _, _) =>
          s"""$id [label="$name" shape=box]\n"""

      }
    }
  }

}
