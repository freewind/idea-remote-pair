package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.fileEditor.FileDocumentManager

class GetDocumentManager {
  def apply(): FileDocumentManager = FileDocumentManager.getInstance()
}
