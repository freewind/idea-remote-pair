package com.thoughtworks.pli.intellij.remotepair.client

import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse

trait ServerStatusHolder {
  this: CurrentProjectHolder =>
  def serverStatus: Option[ServerStatusResponse] = ServerStatusHolder.serverStatus.get(currentProject)
  def serverStatus_=(res: ServerStatusResponse): Unit = ServerStatusHolder.serverStatus += currentProject -> res
}

object ServerStatusHolder {
  var serverStatus: Map[Project, ServerStatusResponse] = Map.empty[Project, ServerStatusResponse]
}
