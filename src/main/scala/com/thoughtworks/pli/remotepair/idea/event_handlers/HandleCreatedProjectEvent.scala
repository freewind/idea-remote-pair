package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreatedProjectEvent
import com.thoughtworks.pli.remotepair.idea.core._

class HandleCreatedProjectEvent(getOpenedFiles: GetOpenedFiles, publishCreateDocumentEvent: PublishCreateDocumentEvent) {

  def apply(event: CreatedProjectEvent) = {
    getOpenedFiles().foreach(publishCreateDocumentEvent.apply)
  }

}
