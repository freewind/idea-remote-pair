package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

class GetMyClientId(currentProject: RichProject) {
  def apply(): Option[String] = currentProject.clientInfo.map(_.clientId)
}
