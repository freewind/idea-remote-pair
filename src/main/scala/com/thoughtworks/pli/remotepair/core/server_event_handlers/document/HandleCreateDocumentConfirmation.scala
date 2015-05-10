package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.ClientVersionedDocuments
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDocumentConfirmation(writeToProjectFile: WriteToProjectFile, runWriteAction: RunWriteAction, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: CreateDocumentConfirmation): Unit = runWriteAction {
    if (clientVersionedDocuments.find(event.path).isEmpty) {
      clientVersionedDocuments.create(event)
      writeToProjectFile(event.path, event.content)
    }
  }

}
