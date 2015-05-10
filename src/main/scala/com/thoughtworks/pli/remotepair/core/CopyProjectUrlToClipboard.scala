package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.idea.settings.ProjectUrlInProjectStorage

class CopyProjectUrlToClipboard(projectUrlInProjectStorage: ProjectUrlInProjectStorage, copyToClipboard: CopyToClipboard) {
  def apply(): Unit = {
    projectUrlInProjectStorage.load() match {
      case Some(url) => copyToClipboard(url)
      case _ =>
    }
  }
}
