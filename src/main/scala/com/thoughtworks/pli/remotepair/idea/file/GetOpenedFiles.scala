package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class GetOpenedFiles(getFileEditorManager: GetFileEditorManager) {
  def apply(): Seq[IdeaFileImpl] = getFileEditorManager().getOpenFiles.toSeq.map(new IdeaFileImpl(_))
}
