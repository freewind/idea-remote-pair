package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, DocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

// FIXME add test
class HandleDocumentSnapshotEvent(currentProject: MyProject, clientVersionedDocuments: ClientVersionedDocuments, logger: PluginLogger, myPlatform: MyIde) {

  def apply(event: DocumentSnapshotEvent): Unit = {
    logger.info(s"before apply event($event), documents: $clientVersionedDocuments")
    clientVersionedDocuments.create(CreateDocumentConfirmation(event.path, event.version, event.content))
    myPlatform.runWriteAction {
      currentProject.getTextEditorsOfPath(event.path) match {
        case Nil => currentProject.findOrCreateFile(event.path).setContent(event.content.text)
        case editors => editors.foreach(_.document.modifyTo(event.content.text))
      }
      currentProject.getFileByRelative(event.path).foreach(file =>
        myPlatform.invokeLater(currentProject.openFileInTab(file))
      )
    }
    logger.info(s"after apply event($event), documents: $clientVersionedDocuments")
  }

}
