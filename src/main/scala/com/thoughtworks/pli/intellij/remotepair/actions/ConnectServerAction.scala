package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialog
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class ConnectServerAction extends AnAction with InvokeLater with CurrentProjectHolder {

  def actionPerformed(event: AnActionEvent) {
    val dialog = createConnectServerDialog()
    dialog.show()
  }

  def createConnectServerDialog() = new ConnectServerDialog(currentProject)

}
