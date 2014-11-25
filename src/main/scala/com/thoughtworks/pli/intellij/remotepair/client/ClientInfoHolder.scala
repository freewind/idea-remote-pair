package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.{ClientInfoResponse, ProjectInfoData, RichProject}

trait ClientInfoHolder {
  this: CurrentProjectHolder =>

  def clientInfo: Option[ClientInfoResponse] = ClientInfoHolder.clientInfo.get(currentProject)
  def clientInfo_=(info: Option[ClientInfoResponse]) = info match {
    case Some(res) => ClientInfoHolder.clientInfo += (currentProject -> res)
    case _ => ClientInfoHolder.clientInfo -= currentProject
  }

  def projectInfo: Option[ProjectInfoData] = for {
    server <- currentProject.serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

}

object ClientInfoHolder {
  var clientInfo: Map[RichProject, ClientInfoResponse] = Map.empty[RichProject, ClientInfoResponse]
}
