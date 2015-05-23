package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.intellij.remotepair.utils.{NewUuid, Md5}
import org.apache.commons.lang.StringUtils

class MyUtils {

  val md5 = new Md5

  val newUuid = new NewUuid

  def isSubPath(path: String, parent: String): Boolean = {
    val (p1, p2) = (standardizePath(path), standardizePath(parent))
    p1 == p2 || p1.startsWith(p2 + "/")
  }

  def standardizePath(path: String) = StringUtils.stripEnd(path, "./")

}
