package ee.mn8.castanet

/*

object Transito {
  import scala.quoted._

  /*inline*/ def transit1: Unit = println(s"splitting ")
  /*inline*/ def transit2: Unit = println(s"joining")
  /*inline*/ def transit3: Unit = println(s"continuing")

  ///*inline*/ def transition(/*inline*/ expr: Any): Unit =  ${transitionImpl('expr)}

  private def transitionImpl(expr: Expr[Any])(using Quotes): Expr[Unit] =
    '{ println("transition stage " + ${Expr(expr.show)} + " is " + $expr) }
}
*/
