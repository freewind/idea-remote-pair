package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocument
import com.thoughtworks.pli.remotepair.core.models.MyFile

class PublishCreateDocumentEvent(myClient: MyClient) {

  def apply(file: MyFile): Unit = file.relativePath.foreach { path =>
    myClient.publishEvent(CreateDocument(path, file.content))
  }

}
