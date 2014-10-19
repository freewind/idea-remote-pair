package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse

trait ServerStatusHolder {
  def serverStatus: Option[ServerStatusResponse] = ClientObjects.serverStatus
  def serverStatus_=(res: ServerStatusResponse): Unit = ClientObjects.serverStatus = Some(res)
}
