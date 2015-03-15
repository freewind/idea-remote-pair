package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.{Document, Editor}

class GetDocumentContent {
  def apply(editor: Editor): String = apply(editor.getDocument)
  def apply(event: DocumentEvent): String = apply(event.getDocument)
  def apply(document: Document): String = document.getCharsSequence.toString
}
