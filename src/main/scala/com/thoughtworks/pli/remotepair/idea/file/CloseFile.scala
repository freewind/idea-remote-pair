package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class CloseFile(getFileEditorManager: GetFileEditorManager) {
  def apply(file: IdeaFileImpl): Unit = {
    getFileEditorManager().closeFile(file.raw)
  }
}
