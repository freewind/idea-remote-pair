package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.idea.core.{WriteToProjectFile, ClientVersionedDocuments}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDocumentConfirmation(writeToProjectFile: WriteToProjectFile, runWriteAction: RunWriteAction, versionedDocuments: ClientVersionedDocuments) {
  def apply(event: CreateDocumentConfirmation): Unit = runWriteAction {
    val doc = versionedDocuments.getOrCreate(event.path)
    doc.synchronized {
      doc.handleCreation(event) match {
        case Some(content) => writeToProjectFile(event.path, content)
        case _ => // do nothing
      }
    }
  }

}
