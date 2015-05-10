package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileEditorManager

class IsFileInActiveTab(getFileEditorManager: GetFileEditorManager) {

  def apply(file: MyFile): Boolean = {
    getFileEditorManager().getSelectedFiles.contains(file)
  }

}
