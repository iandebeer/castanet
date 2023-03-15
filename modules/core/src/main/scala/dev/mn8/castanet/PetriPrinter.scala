package dev.mn8.castanet

 import java.io.PrintWriter
 import java.io.File
 import scala.collection.immutable.ListSet
 import scodec.bits.BitVector

case class PetriPrinter(path: String = "./", fileName:  String = "petrinet", petriNet: ColouredPetriNet) :
  
  /**
   * Creates an output dot file and uses that to create graphviz png output using following command
   * dot -Tpng <filePrefix>.dot -o <filePrefix>.png
   * If you want to change the certain format, change below.
   */
  def print(markers: Option[Markers] = None, steps: Option[Map[ArcId, Long]] = None) = 
    //the places and 
    val index = petriNet.elements.keySet.toList
    val builder: StringBuilder = petriNet.elements.foldLeft(new StringBuilder( "digraph G {\n"))(
      (b,kv) => b.append(kv._2 match 
          case p:Place => 
            val markerString = markers match
              case Some(m) => 
                val up = m.state(p.id).populationCount
                val down = p.capacity - up
                ("•"*up.toInt) + ("_"*down.toInt)

              case None => "°"* p.capacity
            s"""${index.indexOf(p.id)} [label="${p.name}\\n${markerString}\\n" shape=circle]\n"""
          case t:Transition => s"""${index.indexOf(t.id)} [label="${t.name}" shape=box]\n"""
        ))
    
    // the arcs
    petriNet.graph.foldLeft(builder.append("\n"))(
      (b,kv) => b.append(kv._2.toIndexedSeq.reverse.map(l => 
          val stp = if steps.getOrElse(Map[ArcId, BitVector]()).contains(ArcId(kv._1,l.id)) then
             ",color=red,penwidth=3.0"
          else ""   
          s"""${index.indexOf(kv._1)} -> ${index.indexOf(l.id)} [label="${petriNet.arcs(ArcId(kv._1,l.id))}" $stp] \n"""
        ).mkString)
    )
    builder.append("}")
    writeTextToFile(fileName + ".dot", builder.toString)

    val command = new StringBuilder()
    command.append("dot -Tpng:cairo:gd ").    // output type
            append(path + fileName).append(".dot ").   // input dot file
            append("-o ").append(path + fileName).append(".png")  // output image
    executeCommand(command.toString()) 
    
  def executeCommand(command: String) : Unit = 
    Runtime.getRuntime().exec(command)
 
  def writeTextToFile(fileName:String, text: String ): Unit =
    val writer = new PrintWriter(new File(path +fileName))
    writer.write(text)
    writer.close()