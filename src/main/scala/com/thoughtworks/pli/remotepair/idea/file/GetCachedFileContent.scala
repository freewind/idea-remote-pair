package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class GetCachedFileContent(getFileContent: GetFileContent, getDocumentContent: GetDocumentContent) {
  def apply(file: IdeaFileImpl): Option[Content] = {
    val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(file.raw)
    Option(cachedDocument).map(getDocumentContent.apply).map(Content(_, file.getCharset.name()))
  }
}
