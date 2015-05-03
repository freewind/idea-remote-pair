package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocument

class PublishCreateDocumentEvent(publishEvent: PublishEvent, getRelativePath: GetRelativePath, getFileContent: GetFileContent) {

  def apply(file: VirtualFile): Unit = getRelativePath(file).foreach { path =>
    publishEvent(CreateDocument(path, getFileContent(file)))
  }

}
