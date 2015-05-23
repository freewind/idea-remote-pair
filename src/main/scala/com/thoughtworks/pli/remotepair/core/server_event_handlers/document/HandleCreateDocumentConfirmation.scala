package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

case class HandleCreateDocumentConfirmation(currentProject: MyProject, myPlatform: MyIde, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: CreateDocumentConfirmation): Unit = myPlatform.runWriteAction {
    if (clientVersionedDocuments.find(event.path).isEmpty) {
      clientVersionedDocuments.create(event)
      currentProject.getTextEditorsOfPath(event.path) match {
        case Nil => currentProject.findOrCreateFile(event.path).setContent(event.content.text)
        case editors => editors.foreach(_.document.modifyTo(event.content.text))
      }
    }
  }

}
