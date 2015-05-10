package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.RuntimeAssertions
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.GetProjectBaseDir

class FindOrCreateDir(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir, createChildDirectory: CreateChildDirectory, findChild: FindChild) {

  def apply(relativePath: String): IdeaFileImpl = {
    relativePath.split("/").filter(_.length > 0).foldLeft(getProjectBaseDir()) {
      case (file, name) =>
        file.findChild(name).fold(createChildDirectory(file, name))(identity)
    }
  }
}
