package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GetProjectBaseDir(currentProject: Project) {
  def apply(): VirtualFile = currentProject.getBaseDir
}
