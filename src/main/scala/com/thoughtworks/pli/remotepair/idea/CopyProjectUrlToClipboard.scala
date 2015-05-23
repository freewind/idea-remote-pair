package com.thoughtworks.pli.remotepair.idea

import com.thoughtworks.pli.remotepair.core.MySystem
import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage

class CopyProjectUrlToClipboard(myProjectStorage: MyProjectStorage, mySystem: MySystem) {
  def apply(): Unit = {
    myProjectStorage.projectUrl match {
      case Some(url) => mySystem.copyToClipboard(url)
      case _ =>
    }
  }
}
