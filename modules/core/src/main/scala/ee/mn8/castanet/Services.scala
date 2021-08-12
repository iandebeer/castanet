package ee.mn8.castanet

import scala.io.Source
import scala.quoted.*
import cats.data.State

sealed trait ProtoLine
case class Service(packageName: String = "", name: String = "", rpcs: List[RPC] = List())
    extends ProtoLine
case class RPC(name: String, input: String, output: String) extends ProtoLine {
  def toFunction[I, O](): Function1[State[Markers,I], State[Markers,O]] = ???
}

 /*object Services:
  inline def apply[T](x: T)(using ToExpr[T])(using Quotes): Expr[Seq[ee.mn8.castanet.Service]] = ???


  inline def debugSingle(inline expr: Any): Unit = ${debugSingleImpl('expr)} 
  
  private def debugSingleImpl(expr: Expr[Any])(using Quotes): Expr[Unit] =
    '{ println("Value of " + ${Expr(expr.show)} + " is " + $expr)}

  inline def callsFromPaths(inline filePaths: Seq[String]): Unit = 
    val expr: Expr[Seq[Service]] = Expr(extractServiceDetail(filePaths))
    ${callsImpl(expr)} 

  inline def calls(inline serviceDetail: Seq[Service]): Unit = 
    val expr: Expr[Seq[Service]] = Expr(serviceDetail)
    ${callsImpl(expr)} 
  
  def callsImpl(using qtcx: Quotes)(expr: Expr[Seq[Service]]) = 
    import quotes.reflect.*
    val detail = "testing"
    println(detail)


  def extractServiceDetail(filePaths: Seq[String]): Seq[Service] =
    val detail = filePaths.map(s =>
      Source
        .fromFile(s)
        .getLines
        .collect {
          case l: String if l.trim.startsWith("package") =>
            s"${l.trim.replace(";", "").split(" ")(1)}.${s.split("/").last.replace(".proto", "")}"
          case l: String if l.trim.startsWith("service") =>
            Service(name = l.trim.split(" ")(1))
          case l: String if l.trim.startsWith("rpc") =>
            val parts = l
              .replaceAll("[\\(){};]", "")
              .replace("stream", "")
              .replace("rpc", "")
              .replace("returns", "")
              .trim
              .split("\\s+")
            RPC(parts.head, parts(1), parts(2))
        }
        .foldLeft(Service())((service, l) =>
          l match {
            case p: String   => service.copy(packageName = p)
            case s1: Service => service.copy(name = s1.name)
            case r: RPC      => service.copy(rpcs = service.rpcs ++ List(r))
          }
        )
    )
    detail */