package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.Content

class GetCachedFileContent(getFileContent: GetFileContent, getDocumentContent: GetDocumentContent) {
  def apply(file: VirtualFile): Option[Content] = {
    val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(file)
    Option(cachedDocument).map(getDocumentContent.apply).map(Content(_, file.getCharset.name()))
  }
}
