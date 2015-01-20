package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.idea.core.CurrentProjectHolder

trait ClientNameGetter {
  this: CurrentProjectHolder =>

  object ClientName {
    def unapply(clientId: String): Option[String] = {
      currentProject.projectInfo.flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
    }
  }


}
