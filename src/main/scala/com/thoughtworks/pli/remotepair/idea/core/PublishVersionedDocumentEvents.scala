package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}

trait PublishVersionedDocumentEvents extends PublishEvents {
  this: CurrentProjectHolder =>

  def publishCreateDocumentEvent(file: VirtualFile): Unit = {
    val path = currentProject.getRelativePath(file)

    if (currentProject.clientInfo.exists(_.isMaster)) {
      publishEvent(CreateDocument(path, currentProject.getFileContent(file)))
    } else {
      publishEvent(CreateServerDocumentRequest(path))
    }
  }
}
