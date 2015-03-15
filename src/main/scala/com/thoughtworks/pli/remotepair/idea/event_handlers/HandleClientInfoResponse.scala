package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
import com.thoughtworks.pli.remotepair.idea.core.ClientInfoHolder

case class HandleClientInfoResponse(clientInfoHolder: ClientInfoHolder) {
  def apply(event: ClientInfoResponse) {
    clientInfoHolder.put(Some(event))
  }
}
