package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class IsFileOpened(getFileEditorManager: GetFileEditorManager) {
  def apply(file: VirtualFile) = getFileEditorManager().isFileOpen(file)
}
