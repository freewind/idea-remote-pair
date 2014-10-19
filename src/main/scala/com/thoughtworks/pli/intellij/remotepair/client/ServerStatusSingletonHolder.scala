package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.{ServerStatusHolder, ServerStatusResponse}

trait ServerStatusSingletonHolder extends ServerStatusHolder {
  override def serverStatus: Option[ServerStatusResponse] = ClientObjects.serverStatus
  override def serverStatus_=(res: ServerStatusResponse): Unit = ClientObjects.serverStatus = Some(res)
}
