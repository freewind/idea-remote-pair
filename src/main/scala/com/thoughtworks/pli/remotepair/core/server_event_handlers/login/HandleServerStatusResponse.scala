package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.core.ServerStatusHolder

class HandleServerStatusResponse(serverStatusHolder: ServerStatusHolder) {
  def apply(res: ServerStatusResponse) {
    serverStatusHolder.put(Some(res))
  }
}
