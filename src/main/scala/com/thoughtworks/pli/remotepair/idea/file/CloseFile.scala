package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class CloseFile(getFileEditorManager: GetFileEditorManager) {
  def apply(file: VirtualFile): Unit = {
    getFileEditorManager().closeFile(file)
  }
}
