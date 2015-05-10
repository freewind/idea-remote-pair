package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.RuntimeAssertions
import com.thoughtworks.pli.remotepair.idea.file.FindOrCreateDir

class FindOrCreateFile(currentProject: MyProject, findOrCreateDir: FindOrCreateDir, runtimeAssertions: RuntimeAssertions) {

  import runtimeAssertions._

  def apply(relativePath: String): VirtualFile = {
    assume(goodPath(relativePath))
    val pathItems = relativePath.split("/")
    findOrCreateDir(pathItems.init.mkString("/")).findOrCreateChildData(this, pathItems.last)
  }
}
