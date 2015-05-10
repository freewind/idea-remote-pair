package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.RuntimeAssertions

class GetFileByRelative(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir) {

  import runtimeAssertions.goodPath

  def apply(path: String): Option[VirtualFile] = {
    assume(goodPath(path))
    Option(getProjectBaseDir().findFileByRelativePath(path))
  }

}
