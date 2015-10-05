package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.remotepair.idea.RemotePairProjectComponent

class ConnectServerAction extends AnAction("Connect to server") {

  def actionPerformed(event: AnActionEvent) {
    val module = event.getProject.getUserData(RemotePairProjectComponent.moduleKey)
    val dialog = module.dialogFactories.createConnectServerDialog
    dialog.showOnCenter()
  }

}
