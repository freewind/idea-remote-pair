package com.thoughtworks.pli.remotepair.idea.utils

import org.apache.commons.lang.StringUtils

object Paths {

  def isSubPath(path: String, parent: String): Boolean = {
    val (p1, p2) = (standardize(path), standardize(parent))
    p1 == p2 || p1.startsWith(p2 + "/")
  }

  def standardize(path: String) = StringUtils.stripEnd(path, "./")

}
