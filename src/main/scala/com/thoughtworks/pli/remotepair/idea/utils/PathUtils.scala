package com.thoughtworks.pli.intellij.remotepair.utils

import com.thoughtworks.pli.remotepair.idea.core.RuntimeAssertions
import RuntimeAssertions._

object PathUtils {

  def isSubPathOf(path: String, parent: String) = {
    Seq(path, parent).foreach(p => assume(goodPath(p)))
    (path + "/").startsWith(parent + "/")
  }

}
