package ee.mn8.castanet
import scala.quoted.*

object MyMacro :
  inline def isCaseClass[A]: Boolean = ${ isCaseClassImpl[A] }
  private def isCaseClassImpl[A: Type](using qctx: Quotes) : Expr[Boolean] =
    import qctx.reflect.*
    val sym = TypeRepr.of[A].typeSymbol
    Expr(sym.isClassDef && sym.flags.is(Flags.Trait))
  inline def showTree[A](inline a: A): String = ${showTreeImpl[A]('{ a })}

  def showTreeImpl[A: Type](a: Expr[A])(using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(Printer.TreeStructure.show(a.asTerm))
