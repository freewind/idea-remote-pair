package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.idea.core.ServerStatusHolder

case class HandleServerStatusResponse(serverStatusHolder: ServerStatusHolder) {
  def apply(res: ServerStatusResponse) {
    serverStatusHolder.put(Some(res))
  }
}
