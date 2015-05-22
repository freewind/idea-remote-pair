package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.CreatedProjectEvent

class HandleCreatedProjectEvent() {

  def apply(event: CreatedProjectEvent) = {
    //    getOpenedFiles().foreach(publishCreateDocumentEvent.apply)
  }

}
