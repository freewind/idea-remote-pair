package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.ProjectInfoData
import com.thoughtworks.pli.remotepair.core.{ClientInfoHolder, ServerStatusHolder}

class GetProjectInfoData(serverStatusHolder: ServerStatusHolder, clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[ProjectInfoData] = for {
    server <- serverStatusHolder.get
    client <- clientInfoHolder.get
    p <- server.projects.find(_.name == client.project)
  } yield p

}
