package dev.mn8.castanet

enum Colour(rgb:  Int) {
  case BLACK extends  Colour(0x000000)
  case GRAY   extends Colour(0x575757)
  case RED        extends Colour(0xad2323)
  case BLUE       extends Colour(0x2a4bd7)
  case GREEN      extends Colour(0x1d6914)
  case BROWN      extends Colour(0x814a19)
  case PURPLE     extends Colour(0x8126c0)
  case LIGHT_GRAY  extends Colour(0xa0a0a0)
  case LIGHT_GREEN extends Colour(0x81c57a)
  case LIGHT_BLUE  extends Colour(0x9dafff)
  case CYAN       extends Colour(0x410d0d)
  case ORANGE     extends Colour(0xff9233)
  case YELLOW     extends Colour(0xffee33)
  case TAN        extends Colour(0xe9debb)
  case PINK       extends Colour(0xffcdf3)
  case WHITE      extends Colour(0xffffff)

  def toHex: String = Integer.toHexString(rgb) 
}

object Colour:
  def get(ord:Int): (String,String) = 
    if ord < Colour.values.size then
      (Colour.fromOrdinal(ord).toString,Colour.fromOrdinal(ord).toHex)
    else
      (s"COLOUR$ord", "%06x".format(0))   