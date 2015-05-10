package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.thoughtworks.pli.remotepair.idea.models.{IdeaFileImpl, IdeaProjectImpl}

class GetOpenFileDescriptor(currentProject: IdeaProjectImpl) {
  def apply(file: IdeaFileImpl) = new OpenFileDescriptor(currentProject.raw, file.raw)
}
