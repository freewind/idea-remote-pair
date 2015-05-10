package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.FileDocumentManager

class GetDocumentManager {
  def apply(): FileDocumentManager = FileDocumentManager.getInstance()
}
