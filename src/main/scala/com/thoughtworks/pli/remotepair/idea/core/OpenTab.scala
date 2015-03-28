package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class OpenTab(getOpenFileDescriptor: GetOpenFileDescriptor, invokeLater: InvokeLater) {
  def apply(file: VirtualFile): Unit = {
    val openFileDescriptor = getOpenFileDescriptor(file)
    if (openFileDescriptor.canNavigate) {
      invokeLater(openFileDescriptor.navigate(true))
    }
  }
}
