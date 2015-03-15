package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}

case class PublishCreateDocumentEvent(publishEvent: PublishEvent, getRelativePath: GetRelativePath, clientInfoHolder: ClientInfoHolder, getFileContent: GetFileContent) {

  def apply(file: VirtualFile): Unit = getRelativePath(file).foreach { path =>
    if (clientInfoHolder.get.exists(_.isMaster)) {
      publishEvent(CreateDocument(path, getFileContent(file)))
    } else {
      publishEvent(CreateServerDocumentRequest(path))
    }
  }

}
