package com.thoughtworks.pli.remotepair.idea

import com.thoughtworks.pli.remotepair.core.MySystem
import com.thoughtworks.pli.remotepair.idea.settings.ProjectUrlInProjectStorage

class CopyProjectUrlToClipboard(projectUrlInProjectStorage: ProjectUrlInProjectStorage, mySystem: MySystem) {
  def apply(): Unit = {
    projectUrlInProjectStorage.load() match {
      case Some(url) => mySystem.copyToClipboard(url)
      case _ =>
    }
  }
}
