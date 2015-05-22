package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient

class HandleServerStatusResponse(connectedClient: ConnectedClient) {
  def apply(res: ServerStatusResponse) {
    connectedClient.serverStatusHolder.set(Some(res))
  }
}
