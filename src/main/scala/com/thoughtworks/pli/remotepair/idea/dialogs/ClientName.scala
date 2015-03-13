package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class ClientName(currentProject: RichProject) {
  def unapply(clientId: String): Option[String] = {
    currentProject.projectInfo.flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
  }
}
