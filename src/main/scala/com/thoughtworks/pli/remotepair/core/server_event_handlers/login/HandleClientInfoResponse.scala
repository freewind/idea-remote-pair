package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient

case class HandleClientInfoResponse(connectedClient: ConnectedClient) {
  def apply(event: ClientInfoResponse) {
    connectedClient.clientInfoHolder.set(Some(event))
  }
}
