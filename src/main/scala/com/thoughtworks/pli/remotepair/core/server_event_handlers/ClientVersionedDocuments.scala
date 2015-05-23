package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocumentConfirmation
import com.thoughtworks.pli.remotepair.core.ProjectScopeValue
import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyProject}

class ClientVersionedDocuments(myProject: MyProject, clientVersionedDocumentFactory: ClientVersionedDocument.Factory) {
  private val key = new DataKey[Map[String, ClientVersionedDocument]](getClass.getName)
  private val documents = new ProjectScopeValue(myProject, key, Map.empty[String, ClientVersionedDocument])

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
