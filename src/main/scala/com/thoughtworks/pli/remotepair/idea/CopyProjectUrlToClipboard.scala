package com.thoughtworks.pli.remotepair.idea

import com.thoughtworks.pli.remotepair.core.CopyToClipboard
import com.thoughtworks.pli.remotepair.idea.settings.ProjectUrlInProjectStorage

class CopyProjectUrlToClipboard(projectUrlInProjectStorage: ProjectUrlInProjectStorage, copyToClipboard: CopyToClipboard) {
  def apply(): Unit = {
    projectUrlInProjectStorage.load() match {
      case Some(url) => copyToClipboard(url)
      case _ =>
    }
  }
}
