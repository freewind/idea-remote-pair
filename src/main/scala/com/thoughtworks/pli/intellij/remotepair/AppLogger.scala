package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.diagnostic.Logger

trait AppLogger {

  val log = Logger.getInstance(this.getClass)

}
