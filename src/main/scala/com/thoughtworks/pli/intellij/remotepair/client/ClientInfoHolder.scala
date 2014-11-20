package com.thoughtworks.pli.intellij.remotepair.client

import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{ProjectInfoData, ClientInfoResponse}

trait ClientInfoHolder extends ServerStatusHolder {
  this: CurrentProjectHolder =>

  def clientInfo: Option[ClientInfoResponse] = ClientInfoHolder.clientInfo.get(currentProject)
  def clientInfo_=(info: Option[ClientInfoResponse]) = info match {
    case Some(res) => ClientInfoHolder.clientInfo += (currentProject -> res)
    case _ => ClientInfoHolder.clientInfo -= currentProject
  }

  def projectInfo: Option[ProjectInfoData] = for {
    server <- serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

}

object ClientInfoHolder {
  var clientInfo: Map[Project, ClientInfoResponse] = Map.empty[Project, ClientInfoResponse]
}