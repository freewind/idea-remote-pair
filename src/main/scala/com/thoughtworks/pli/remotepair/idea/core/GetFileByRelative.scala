package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class GetFileByRelative(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir) {

  import runtimeAssertions.goodPath

  def apply(path: String): Option[VirtualFile] = {
    assume(goodPath(path))
    Option(getProjectBaseDir().findFileByRelativePath(path))
  }

}
