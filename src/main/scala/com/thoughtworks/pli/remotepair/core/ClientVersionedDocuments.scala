package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation

object ClientVersionedDocuments {
  val Key = new Key[Map[String, ClientVersionedDocument]](ClientVersionedDocuments.getClass.getName)
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
