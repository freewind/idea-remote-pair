package com.thoughtworks.pli.intellij.remotepair

import org.slf4j.LoggerFactory

trait AppLogger {

  val log = LoggerFactory.getLogger(this.getClass)

}
