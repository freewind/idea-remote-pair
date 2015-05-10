package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetOpenFileDescriptor(currentProject: IdeaProjectImpl) {
  def apply(file: VirtualFile) = new OpenFileDescriptor(currentProject.raw, file)
}
