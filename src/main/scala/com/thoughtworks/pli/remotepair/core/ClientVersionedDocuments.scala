package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.models.MyProject.ProjectKey

object ClientVersionedDocuments {
  val Key = new ProjectKey[Map[String, ClientVersionedDocument]](getClass.getName)
}

class ClientVersionedDocuments(clientVersionedDocumentFactory: ClientVersionedDocument.Factory, currentProjectScope: CurrentProjectScope) {
  private val documents = currentProjectScope.value(ClientVersionedDocuments.Key, Map.empty[String, ClientVersionedDocument])

  def find(path: String): Option[ClientVersionedDocument] = synchronized(documents.get.get(path))

  def create(event: CreateDocumentConfirmation): ClientVersionedDocument = synchronized {
    val doc = clientVersionedDocumentFactory.apply(event)
    documents.set(documents.get + (doc.path -> doc))
    doc
  }

  override def toString: String = {
    documents.toString
  }
}
