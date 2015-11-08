package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.remotepair.core.ProjectScopeValue
import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyProject}

class ClientVersionedDocuments(myProject: MyProject, clientVersionedDocumentFactory: ClientVersionedDocument.Factory) {
  private val key = new DataKey[Map[String, ClientVersionedDocument]](getClass.getName)
  private var documents = createEmptyDocuments()

  def find(path: String): Option[ClientVersionedDocument] = synchronized(documents.get.get(path))

  def create(docInfo: DocumentInfo): ClientVersionedDocument = synchronized {
    val doc = clientVersionedDocumentFactory.apply(docInfo)
    documents.set(documents.get + (doc.path -> doc))
    doc
  }

  def clear() = synchronized(documents = createEmptyDocuments())

  private def createEmptyDocuments() = new ProjectScopeValue(myProject, key, Map.empty[String, ClientVersionedDocument])

  override def toString: String = {
    documents.toString
  }
}
