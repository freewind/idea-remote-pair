package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class IsFileOpened(getFileEditorManager: GetFileEditorManager) {
  def apply(file: IdeaFileImpl) = getFileEditorManager().isFileOpen(file.raw)
}
