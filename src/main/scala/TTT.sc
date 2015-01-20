val x: PartialFunction[String, Int] = {
  case s: String => 11
}

val y: PartialFunction[String, Int] = {
  case s: String => 11
}

x == x
x == y

val list = Seq(x, y)

list.filterNot(_ == x)

