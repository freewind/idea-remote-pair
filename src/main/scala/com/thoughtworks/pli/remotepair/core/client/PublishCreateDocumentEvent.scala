package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocument
import com.thoughtworks.pli.remotepair.core.models.MyFile

class PublishCreateDocumentEvent(connectedClient: ConnectedClient) {

  def apply(file: MyFile): Unit = file.relativePath.foreach { path =>
    connectedClient.publishEvent(CreateDocument(path, file.content))
  }

}
