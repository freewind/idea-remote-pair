package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class PublishCreateDocumentEvent(currentProject: RichProject, publishEvent: PublishEvent) {

  def apply(file: VirtualFile): Unit = currentProject.getRelativePath(file).foreach { path =>
    if (currentProject.clientInfo.exists(_.isMaster)) {
      publishEvent(CreateDocument(path, currentProject.getFileContent(file)))
    } else {
      publishEvent(CreateServerDocumentRequest(path))
    }
  }

}
