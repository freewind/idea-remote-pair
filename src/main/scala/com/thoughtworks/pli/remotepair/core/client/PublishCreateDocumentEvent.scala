package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocument
import com.thoughtworks.pli.remotepair.core.models.MyFile

class PublishCreateDocumentEvent(publishEvent: PublishEvent) {

  def apply(file: MyFile): Unit = file.relativePath.foreach { path =>
    publishEvent(CreateDocument(path, file.content))
  }

}
