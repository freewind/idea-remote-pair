package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.FileEditor

class GetEditorsOfPath(getFileByRelative: GetFileByRelative, getFileEditorManager: GetFileEditorManager) {

  def apply(path: String): Seq[FileEditor] = {
    getFileByRelative(path).map(file => getFileEditorManager().getAllEditors(file).toSeq).getOrElse(Nil)
  }
}
