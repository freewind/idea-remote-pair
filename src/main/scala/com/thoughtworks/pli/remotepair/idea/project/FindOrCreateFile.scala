package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core.RuntimeAssertions
import com.thoughtworks.pli.remotepair.idea.file.FindOrCreateDir
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class FindOrCreateFile(currentProject: MyProject, findOrCreateDir: FindOrCreateDir, runtimeAssertions: RuntimeAssertions) {

  import runtimeAssertions._

  def apply(relativePath: String): IdeaFileImpl = {
    assume(goodPath(relativePath))
    val pathItems = relativePath.split("/")
    new IdeaFileImpl(findOrCreateDir(pathItems.init.mkString("/")).raw.findOrCreateChildData(this, pathItems.last))
  }
}
