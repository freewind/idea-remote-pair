package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.{RichProject, ServerStatusResponse}

trait ServerStatusHolder {
  this: CurrentProjectHolder =>
  def serverStatus: Option[ServerStatusResponse] = ServerStatusHolder.serverStatus.get(currentProject)
  def serverStatus_=(res: ServerStatusResponse): Unit = ServerStatusHolder.serverStatus += currentProject -> res
}

object ServerStatusHolder {
  var serverStatus: Map[RichProject, ServerStatusResponse] = Map.empty[RichProject, ServerStatusResponse]
}
