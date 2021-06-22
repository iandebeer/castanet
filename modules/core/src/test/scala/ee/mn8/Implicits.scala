package ee.mn8

case class Person(name: String):
   def greet: String = s"Hey, I'm $name. Scala rocks!"


object Implicits extends App:

  extension(s:String)
    def greet:String = Person(s).greet

  extension(value:Int)
    def double:Int = value * 2
    def triple:Int = value * 3

  println("Ian".greet * 1.double)
  println("Ian".greet + 2.triple)


