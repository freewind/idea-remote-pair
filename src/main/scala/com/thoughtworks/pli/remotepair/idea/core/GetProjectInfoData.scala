package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.ProjectInfoData

class GetProjectInfoData(serverStatusHolder: ServerStatusHolder, clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[ProjectInfoData] = for {
    server <- serverStatusHolder.get
    client <- clientInfoHolder.get
    p <- server.projects.find(_.name == client.project)
  } yield p

}
