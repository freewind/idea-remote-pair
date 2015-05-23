package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.editor.Document
import com.thoughtworks.pli.remotepair.core.models.MyDocument

class IdeaDocumentImpl(document: Document) extends MyDocument {
  override def length: Int = document.getTextLength
  override def setContent(text: String): Unit = document.setText(text)
  override def content: String = document.getText
  override def insertString(offset: Int, newString: String): Unit = document.insertString(offset, newString)
  override def deleteString(offset: Int, length: Int): Unit = document.deleteString(offset, length)
}
