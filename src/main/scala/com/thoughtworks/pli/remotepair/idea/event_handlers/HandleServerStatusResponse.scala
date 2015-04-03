package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.idea.core.ServerStatusHolder

class HandleServerStatusResponse(serverStatusHolder: ServerStatusHolder) {
  def apply(res: ServerStatusResponse) {
    serverStatusHolder.put(Some(res))
  }
}
