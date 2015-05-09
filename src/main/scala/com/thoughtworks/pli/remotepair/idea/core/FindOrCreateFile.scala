package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.models.myfile.FindOrCreateDir

class FindOrCreateFile(currentProject: Project, findOrCreateDir: FindOrCreateDir, runtimeAssertions: RuntimeAssertions) {

  import runtimeAssertions._

  def apply(relativePath: String): VirtualFile = {
    assume(goodPath(relativePath))
    val pathItems = relativePath.split("/")
    findOrCreateDir(pathItems.init.mkString("/")).findOrCreateChildData(this, pathItems.last)
  }
}
