package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile

case class HandleCreateDocumentConfirmation(writeToProjectFile: WriteToProjectFile, myPlatform: MyPlatform, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: CreateDocumentConfirmation): Unit = myPlatform.runWriteAction {
    if (clientVersionedDocuments.find(event.path).isEmpty) {
      clientVersionedDocuments.create(event)
      writeToProjectFile(event.path, event.content)
    }
  }

}
