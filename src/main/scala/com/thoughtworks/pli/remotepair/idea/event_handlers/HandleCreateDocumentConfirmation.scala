package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.ClientVersionedDocuments
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDocumentConfirmation(currentProject: RichProject, runWriteAction: RunWriteAction, versionedDocuments: ClientVersionedDocuments) {
  def apply(event: CreateDocumentConfirmation): Unit = runWriteAction {
    val doc = versionedDocuments.getOrCreate(currentProject, event.path)
    doc.synchronized {
      doc.handleCreation(event) match {
        case Some(content) => currentProject.smartSetContentTo(event.path, content)
        case _ => // do nothing
      }
    }
  }

}
