package com.thoughtworks.pli.remotepair.effects

import scalaz.Scalaz._
import scalaz._

trait Interpreter extends (AppAction ~> Id) {
  def apply[A](action: AppAction[A]): A
}

