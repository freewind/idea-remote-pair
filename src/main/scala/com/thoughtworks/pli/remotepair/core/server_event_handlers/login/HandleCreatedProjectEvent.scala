package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.CreatedProjectEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.PublishCreateDocumentEvent
import com.thoughtworks.pli.remotepair.idea.file.GetOpenedFiles

class HandleCreatedProjectEvent(getOpenedFiles: GetOpenedFiles, publishCreateDocumentEvent: PublishCreateDocumentEvent) {

  def apply(event: CreatedProjectEvent) = {
    //    getOpenedFiles().foreach(publishCreateDocumentEvent.apply)
  }

}
