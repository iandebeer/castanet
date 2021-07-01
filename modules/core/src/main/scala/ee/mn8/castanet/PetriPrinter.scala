package ee.mn8.castanet

 import java.io.PrintWriter
 import java.io.File
 import LinkableElement._
 import scala.collection.immutable.ListSet

 //import scala.lang.PostfixOps
 
case class PetriPrinter(path: String = "/Users/ian/dev/castanet/modules/core/src/test/resource/", fileName:  String = "petrinet", graph: PetriGraph) :
 
  /**
   * Creates an output dot file and uses that to create graphviz png output using following command
   * dot -Tpng <filePrefix>.dot -o <filePrefix>.png
   * If you want to change the certain format, change below.
   */
  def print() = 
    val builder: StringBuilder = graph.foldLeft(new StringBuilder( "digraph G {\n"))(
      (b,kv) => b.append(kv._2.map(l => l match 
          case p:Place => s"""${p.id} [label="${p.name}\\n${"•"* p.capacity}\\n" shape=circle]"""
          case t:Transition => s"""${t.id} [label="${l.name}" shape=box]"""
        ).mkString("\n"))
    )

    graph.foldLeft(builder.append("\n"))(
      (b,kv) => b.append(kv._2.map(l => 
          s"${kv._1} -> ${l.id}\n"
        ).mkString)
    )
    builder.append("}")
    writeTextToFile(fileName + ".dot", builder.toString)

    val command = new StringBuilder()
    command.append("dot -Tpng ").    // output type
            append(path + fileName).append(".dot ").   // input dot file
            append("-o ").append(path + fileName).append(".png")  // output image
    executeCommand(command.toString()) 
    
  def executeCommand(command: String) : Unit = 
    Runtime.getRuntime().exec(command)
 
  def writeTextToFile(fileName:String, text: String ): Unit =
    val writer = new PrintWriter(new File(path +fileName))
    writer.write(text)
    writer.close()