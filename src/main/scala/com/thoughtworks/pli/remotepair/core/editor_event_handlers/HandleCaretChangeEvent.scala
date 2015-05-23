package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.idea.file.GetDocumentContent

class HandleCaretChangeEvent(myClient: MyClient, logger: PluginLogger, getDocumentContent: GetDocumentContent) {

  val KeyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def apply(event: EditorCaretChangeEvent): Unit = {
    if (myClient.isWatching(event.file) && !myClient.isReadonlyMode) {
      val docLength = event.editor.document.length
      if (event.editor.getUserData(KeyDocumentLength).contains(docLength)) {
        for {
          path <- event.file.relativePath
          ee = MoveCaretEvent(path, event.offset)
        } myClient.publishEvent(ee)
      } else {
        event.editor.putUserData(KeyDocumentLength, docLength)
      }
    }
  }
}
