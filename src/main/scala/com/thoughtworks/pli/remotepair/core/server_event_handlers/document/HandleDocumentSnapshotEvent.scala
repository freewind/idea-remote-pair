package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, DocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.GetMyClientName
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile

// FIXME add test
class HandleDocumentSnapshotEvent(currentProject: MyProject, clientVersionedDocuments: ClientVersionedDocuments, logger: PluginLogger, getMyClientName: GetMyClientName, writeToProjectFile: WriteToProjectFile, myPlatform: MyPlatform) {

  def apply(event: DocumentSnapshotEvent): Unit = {
    logger.info(s"before apply event($event), documents: $clientVersionedDocuments")
    clientVersionedDocuments.create(CreateDocumentConfirmation(event.path, event.version, event.content))
    myPlatform.runWriteAction {
      writeToProjectFile(event.path, event.content)
      currentProject.getFileByRelative(event.path).foreach(file =>
        myPlatform.invokeLater(currentProject.openFileInTab(file))
      )
    }
    logger.info(s"after apply event($event), documents: $clientVersionedDocuments")
  }

}
