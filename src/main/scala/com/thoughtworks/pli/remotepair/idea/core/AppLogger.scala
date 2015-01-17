package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger

trait AppLogger {

  val log = Logger.getInstance(this.getClass)

}
