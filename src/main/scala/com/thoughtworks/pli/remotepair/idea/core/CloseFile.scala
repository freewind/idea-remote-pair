package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class CloseFile(getFileEditorManager: GetFileEditorManager) {
  def apply(file: VirtualFile): Unit = {
    getFileEditorManager().closeFile(file)
  }
}
