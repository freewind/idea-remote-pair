package com.thoughtworks.pli.intellij.remotepair.utils

import java.util.UUID

trait UuidSupport {
  def newUuid(): String = UUID.randomUUID().toString
}
