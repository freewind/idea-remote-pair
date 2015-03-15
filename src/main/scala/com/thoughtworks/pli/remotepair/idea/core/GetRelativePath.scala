package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import org.apache.commons.lang.StringUtils


class GetRelativePath(getProjectBasePath: GetProjectBasePath, runtimeAssertions: RuntimeAssertions, isSubPath: IsSubPath, standardizePath: StandardizePath) {

  import runtimeAssertions.goodPath

  def apply(file: VirtualFile): Option[String] = apply(file.getPath)

  def apply(path: String): Option[String] = {
    val base = standardizePath(getProjectBasePath())
    Seq(base, path).foreach(p => assume(goodPath(p)))
    if (isSubPath(path, base)) {
      Some(StringUtils.removeStart(path, base)).filterNot(_.isEmpty).orElse(Some("/"))
    } else {
      None
    }
  }

}
