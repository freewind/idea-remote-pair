package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class OpenFileInTab(getOpenFileDescriptor: GetOpenFileDescriptor, invokeLater: InvokeLater, findOrCreateFile: FindOrCreateFile) {

  def apply(path: String): Unit = apply(findOrCreateFile(path))

  def apply(file: VirtualFile): Unit = {
    val openFileDescriptor = getOpenFileDescriptor(file)
    if (openFileDescriptor.canNavigate) {
      invokeLater(openFileDescriptor.navigate(true))
    }
  }
}
