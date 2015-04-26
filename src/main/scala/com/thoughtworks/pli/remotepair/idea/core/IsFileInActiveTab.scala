package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class IsFileInActiveTab(getFileEditorManager: GetFileEditorManager) {

  def apply(file: VirtualFile): Boolean = {
    getFileEditorManager().getSelectedFiles.contains(file)
  }

}
