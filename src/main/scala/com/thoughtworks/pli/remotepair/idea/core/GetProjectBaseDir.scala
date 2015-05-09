package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GetProjectBaseDir(currentProject: Project) {
  def apply(): VirtualFile = currentProject.getBaseDir
}
