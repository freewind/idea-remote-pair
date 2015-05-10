package com.thoughtworks.pli.remotepair.idea.idea

import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.{FindOrCreateFile, GetOpenFileDescriptor}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class OpenFileInTab(getOpenFileDescriptor: GetOpenFileDescriptor, invokeLater: InvokeLater, findOrCreateFile: FindOrCreateFile) {

  def apply(path: String): Unit = apply(findOrCreateFile(path))

  def apply(file: IdeaFileImpl): Unit = {
    val openFileDescriptor = getOpenFileDescriptor(file)
    if (openFileDescriptor.canNavigate) {
      invokeLater(openFileDescriptor.navigate(true))
    }
  }
}
