package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.CreatedProjectEvent
import com.thoughtworks.pli.remotepair.core.client.PublishCreateDocumentEvent

class HandleCreatedProjectEvent(publishCreateDocumentEvent: PublishCreateDocumentEvent) {

  def apply(event: CreatedProjectEvent) = {
    //    getOpenedFiles().foreach(publishCreateDocumentEvent.apply)
  }

}
