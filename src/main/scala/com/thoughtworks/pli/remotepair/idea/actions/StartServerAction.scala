package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.remotepair.core.DefaultValues
import com.thoughtworks.pli.remotepair.idea.RemotePairProjectComponent

class StartServerAction extends AnAction("Start local server") {

  def actionPerformed(event: AnActionEvent) {
    val module = event.getProject.getUserData(RemotePairProjectComponent.moduleKey)
    module.myServer.start(DefaultValues.DefaultServerPort)
  }

}



