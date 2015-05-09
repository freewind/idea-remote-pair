package com.thoughtworks.pli.remotepair.idea.core.models.myfile

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.GetFileEditorManager

class GetOpenedFiles(getFileEditorManager: GetFileEditorManager) {
  def apply(): Seq[VirtualFile] = getFileEditorManager().getOpenFiles.toSeq
}
