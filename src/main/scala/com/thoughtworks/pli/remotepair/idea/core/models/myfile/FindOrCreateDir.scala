package com.thoughtworks.pli.remotepair.idea.core.models.myfile

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.{CreateChildDirectory, GetProjectBaseDir, RuntimeAssertions}

class FindOrCreateDir(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir, createChildDirectory: CreateChildDirectory, findChild: FindChild) {

  def apply(relativePath: String): VirtualFile = {
    relativePath.split("/").filter(_.length > 0).foldLeft(getProjectBaseDir()) {
      case (file, name) =>
        findChild(file, name).fold(createChildDirectory(file, name))(identity)
    }
  }
}
