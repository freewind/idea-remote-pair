package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath

case class RuntimeAssertions(isSubPath: IsSubPath, logger: PluginLogger) {

  val noWindowsPathSeparator = (path: String) => {
    val ok = !path.contains("\\")
    if (!ok) logger.error(s"Path($path) should not contain Windows path separator(\\)")
    ok
  }

  val noEndingPathSeparator = (path: String) => {
    val ok = !path.endsWith("/")
    if (!ok) logger.error(s"Path($path) should not end with path separator")
    ok
  }

  val hasLeadingPathSeparator = (path: String) => {
    val ok = path.startsWith("/")
    if (!ok) logger.error(s"Path($path) should start with path separator")
    ok
  }


  val goodPath = (path: String) => {
    noWindowsPathSeparator(path) && hasLeadingPathSeparator(path) && noEndingPathSeparator(path)
  }

}
