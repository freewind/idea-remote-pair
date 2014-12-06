package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.utils.PathUtils

object RuntimeAssertions {

  val goodPath = (path: String) => {
    noWindowsPathSeparator(path) && hasLeadingPathSeparator(path) && noEndingPathSeparator(path)
  }

  val noWindowsPathSeparator = (path: String) => {
    val ok = !path.contains("\\")
    if (!ok) println(s"Path($path) should not contain Windows path separator(\\)")
    ok
  }

  val noEndingPathSeparator = (path: String) => {
    val ok = !path.endsWith("/")
    if (!ok) println(s"Path($path) should not end with path separator")
    ok
  }

  val hasLeadingPathSeparator = (path: String) => {
    val ok = path.startsWith("/")
    if (!ok) println(s"Path($path) should start with path separator")
    ok
  }

  val hasParentPath = (path: String, parent: String) => {
    val ok = PathUtils.isSubPathOf(path, parent)
    if (!ok) println(s"Path($path) should have parent path($parent)")
    ok
  }

}
