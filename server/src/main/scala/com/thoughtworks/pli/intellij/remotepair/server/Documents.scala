package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair.{Content, CreateDocument}

class Documents {
  private var docs = Map.empty[String, Document]
  def create(createDoc: CreateDocument): Document = {
    val doc = Document(createDoc.path, Document.InitVersion, createDoc.content)
    docs += (doc.path -> doc)
    doc
  }
  def find(path: String): Option[Document] = docs.get(path)
}


case class Document(path: String, version: Int, content: Content)

object Document {
  val InitVersion = 0
}