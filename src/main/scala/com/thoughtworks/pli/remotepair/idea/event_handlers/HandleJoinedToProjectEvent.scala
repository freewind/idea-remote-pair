package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.idea.core._

case class HandleJoinedToProjectEvent(getOpenedFiles: GetOpenedFiles, publishCreateDocumentEvent: PublishCreateDocumentEvent, applyReadonlyModeToAllOpenEditors: ApplyReadonlyModeToAllOpenEditors) {

  def apply(event: JoinedToProjectEvent): Unit = {
    getOpenedFiles().foreach(publishCreateDocumentEvent.apply)
    applyReadonlyModeToAllOpenEditors()
  }

}
