package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PutUserData, GetUserData, PluginLogger}
import com.thoughtworks.pli.remotepair.core.client.{InWatchingList, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.editor.GetCaretOffset
import com.thoughtworks.pli.remotepair.idea.file.{GetRelativePath, GetDocumentContent}

class HandleCaretChangeEvent(publishEvent: PublishEvent, logger: PluginLogger, inWatchingList: InWatchingList, getDocumentContent: GetDocumentContent, getUserData: GetUserData, putUserData: PutUserData, getRelativePath: GetRelativePath, getCaretOffset: GetCaretOffset, isReadonlyMode: IsReadonlyMode) {

  val KeyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def apply(event: IdeaCaretChangeEvent): Unit = {
    if (inWatchingList(event.file) && !isReadonlyMode()) {
      val docLength = getDocumentContent(event.editor).length()
      if (getUserData(event.editor, KeyDocumentLength).contains(docLength)) {
        for {
          path <- getRelativePath(event.file)
          ee = MoveCaretEvent(path, event.offset)
        } publishEvent(ee)
      } else {
        putUserData(event.editor, KeyDocumentLength, docLength)
      }
    }
  }
}
