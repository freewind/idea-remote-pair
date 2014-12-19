package com.thoughtworks.pli.intellij.remotepair

object ServerLogger {
  var info: String => Unit = scala.Predef.println
}
