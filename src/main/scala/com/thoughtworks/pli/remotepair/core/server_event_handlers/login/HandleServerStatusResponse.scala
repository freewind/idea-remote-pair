package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.core.client.MyClient

class HandleServerStatusResponse(myClient: MyClient) {
  def apply(res: ServerStatusResponse) {
    myClient.serverStatusHolder.set(Some(res))
  }
}
