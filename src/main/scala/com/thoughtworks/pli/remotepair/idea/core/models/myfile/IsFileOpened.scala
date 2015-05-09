package com.thoughtworks.pli.remotepair.idea.core.models.myfile

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.GetFileEditorManager

class IsFileOpened(getFileEditorManager: GetFileEditorManager) {
  def apply(file: VirtualFile) = getFileEditorManager().isFileOpen(file)
}
