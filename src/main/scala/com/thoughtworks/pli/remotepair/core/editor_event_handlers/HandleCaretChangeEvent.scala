package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.client.{InWatchingList, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.editor.GetCaretOffset
import com.thoughtworks.pli.remotepair.idea.file.GetDocumentContent

class HandleCaretChangeEvent(publishEvent: PublishEvent, logger: PluginLogger, inWatchingList: InWatchingList, getDocumentContent: GetDocumentContent, getCaretOffset: GetCaretOffset, isReadonlyMode: IsReadonlyMode) {

  val KeyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def apply(event: EditorCaretChangeEvent): Unit = {
    if (inWatchingList(event.file) && !isReadonlyMode()) {
      val docLength = event.editor.document.length
      if (event.editor.getUserData(KeyDocumentLength).contains(docLength)) {
        for {
          path <- event.file.relativePath
          ee = MoveCaretEvent(path, event.offset)
        } publishEvent(ee)
      } else {
        event.editor.putUserData(KeyDocumentLength, docLength)
      }
    }
  }
}
