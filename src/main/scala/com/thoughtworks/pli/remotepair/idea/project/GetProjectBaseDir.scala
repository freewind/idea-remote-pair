package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.intellij.openapi.vfs.VirtualFile

class GetProjectBaseDir(currentProject: MyProject) {
  def apply(): VirtualFile = currentProject.getBaseDir
}
