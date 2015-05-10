package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class GetOpenedFiles(getFileEditorManager: GetFileEditorManager) {
  def apply(): Seq[VirtualFile] = getFileEditorManager().getOpenFiles.toSeq
}
