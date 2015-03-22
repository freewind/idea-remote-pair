package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.idea.core.{WriteToProjectFile, ClientVersionedDocuments}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDocumentConfirmation(writeToProjectFile: WriteToProjectFile, runWriteAction: RunWriteAction, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: CreateDocumentConfirmation): Unit = runWriteAction {
    if (clientVersionedDocuments.find(event.path).isEmpty) {
      clientVersionedDocuments.create(event)
      writeToProjectFile(event.path, event.content)
    }
  }

}
