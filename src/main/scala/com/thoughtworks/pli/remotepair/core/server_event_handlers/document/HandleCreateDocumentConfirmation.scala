package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.{DocumentInfo, ClientVersionedDocuments}

case class HandleCreateDocumentConfirmation(currentProject: MyProject, myIde: MyIde, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: CreateDocumentConfirmation): Unit = myIde.runWriteAction {
    if (clientVersionedDocuments.find(event.path).isEmpty) {
      clientVersionedDocuments.create(DocumentInfo(event.path, event.version, event.content))
      currentProject.getTextEditorsOfPath(event.path) match {
        case Nil => currentProject.findOrCreateFile(event.path).setContent(event.content.text)
        case editors => editors.foreach(_.document.modifyTo(event.content.text))
      }
    }
  }

}
