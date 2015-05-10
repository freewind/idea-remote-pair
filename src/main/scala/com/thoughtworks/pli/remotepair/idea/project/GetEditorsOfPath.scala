package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.FileEditor
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetEditorsOfPath(currentProject: IdeaProjectImpl, getFileEditorManager: GetFileEditorManager) {

  def apply(path: String): Seq[FileEditor] = {
    currentProject.getFileByRelative(path).map(file => getFileEditorManager().getAllEditors(file.rawFile).toSeq).getOrElse(Nil)
  }
}
