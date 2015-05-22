package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.models.MyProject

class ClientVersionedDocuments(myProject: MyProject, clientVersionedDocumentFactory: ClientVersionedDocument.Factory) {
  private val documents = new ProjectScopeValue(myProject, getClass.getName, Map.empty[String, ClientVersionedDocument])

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
