package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.util.Key
import com.intellij.openapi.editor.Editor

class DocumentListenerSupport extends ListenerManageSupport[DocumentListener] {
  val key = new Key[DocumentListener]("remote_pair.listeners.document")

  def createNewListener(): DocumentListener = {
    new DocumentListener {
      override def documentChanged(event: DocumentEvent) {
        println("## documentChanged: " + event)
      }

      override def beforeDocumentChange(event: DocumentEvent) {
        println("## beforeDocumentChanged: " + event)
      }
    }
  }

  override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

  override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener

}
