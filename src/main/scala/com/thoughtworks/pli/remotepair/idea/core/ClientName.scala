package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

case class ClientName(currentProject: RichProject) {
  def unapply(clientId: String): Option[String] = {
    currentProject.projectInfo.flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
  }
}
