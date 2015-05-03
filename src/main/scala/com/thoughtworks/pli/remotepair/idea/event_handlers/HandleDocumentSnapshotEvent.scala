package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, DocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

// FIXME add test
class HandleDocumentSnapshotEvent(clientVersionedDocuments: ClientVersionedDocuments, logger: PluginLogger, getMyClientName: GetMyClientName, writeToProjectFile: WriteToProjectFile, runWriteAction: RunWriteAction, openFileInTab: OpenFileInTab) {

  def apply(event: DocumentSnapshotEvent): Unit = {
    logger.info(s"before apply event($event), documents: $clientVersionedDocuments")
    clientVersionedDocuments.create(CreateDocumentConfirmation(event.path, event.version, event.content))
    runWriteAction {
      writeToProjectFile(event.path, event.content)
      openFileInTab(event.path)
    }
    logger.info(s"after apply event($event), documents: $clientVersionedDocuments")
  }

}
