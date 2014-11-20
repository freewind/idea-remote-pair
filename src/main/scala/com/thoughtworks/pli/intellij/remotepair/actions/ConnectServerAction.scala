package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialog

class ConnectServerAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val dialog = createConnectServerDialog(event.getProject)
    dialog.show()
  }

  def createConnectServerDialog(project: Project) = new ConnectServerDialog(project)

}
