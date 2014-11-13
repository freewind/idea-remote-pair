package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse

trait ServerStatusHolder {
  def serverStatus: Option[ServerStatusResponse] = ServerStatusHolder.serverStatus
  def serverStatus_=(res: ServerStatusResponse): Unit = ServerStatusHolder.serverStatus = Some(res)
}

object ServerStatusHolder {
  var serverStatus: Option[ServerStatusResponse] = None
}
