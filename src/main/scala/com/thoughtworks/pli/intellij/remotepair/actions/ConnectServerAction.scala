package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialogProvider

class ConnectServerAction extends AnAction with InvokeLater with ConnectServerDialogProvider {

  def actionPerformed(event: AnActionEvent) {
    val dialog = createConnectServerDialog()
    dialog.show()
  }

}
