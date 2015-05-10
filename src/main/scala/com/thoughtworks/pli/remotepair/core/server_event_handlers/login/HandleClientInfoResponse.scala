package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
import com.thoughtworks.pli.remotepair.core.ClientInfoHolder

case class HandleClientInfoResponse(clientInfoHolder: ClientInfoHolder) {
  def apply(event: ClientInfoResponse) {
    clientInfoHolder.put(Some(event))
  }
}
