package com.thoughtworks.pli.intellij.remotepair

import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer

object JsonFormats {
  implicit val formats = DefaultFormats + new EnumNameSerializer(WorkingMode)
}
