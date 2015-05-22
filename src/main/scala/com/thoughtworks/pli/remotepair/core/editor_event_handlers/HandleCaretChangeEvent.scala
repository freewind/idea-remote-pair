package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.idea.editor.GetCaretOffset
import com.thoughtworks.pli.remotepair.idea.file.GetDocumentContent

class HandleCaretChangeEvent(connectedClient: ConnectedClient, logger: PluginLogger, getDocumentContent: GetDocumentContent, getCaretOffset: GetCaretOffset) {

  val KeyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def apply(event: EditorCaretChangeEvent): Unit = {
    if (connectedClient.isWatching(event.file) && !connectedClient.isReadonlyMode) {
      val docLength = event.editor.document.length
      if (event.editor.getUserData(KeyDocumentLength).contains(docLength)) {
        for {
          path <- event.file.relativePath
          ee = MoveCaretEvent(path, event.offset)
        } connectedClient.publishEvent(ee)
      } else {
        event.editor.putUserData(KeyDocumentLength, docLength)
      }
    }
  }
}
