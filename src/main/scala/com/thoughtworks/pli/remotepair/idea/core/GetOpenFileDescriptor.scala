package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GetOpenFileDescriptor(currentProject: Project) {
  def apply(file: VirtualFile) = new OpenFileDescriptor(currentProject, file)
}
