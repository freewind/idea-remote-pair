package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

class GetRelativePath(currentProject: RichProject) {
  def apply(file: VirtualFile): Option[String] = currentProject.getRelativePath(file)
}
