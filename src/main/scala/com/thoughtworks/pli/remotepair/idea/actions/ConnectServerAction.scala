package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.idea.core.InvokeLater
import com.thoughtworks.pli.remotepair.idea.dialogs.ConnectServerDialog

class ConnectServerAction extends AnAction("Connect to server") with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val dialog = createDialog(event.getProject)
    dialog.showOnCenter()
  }

  def createDialog(project: Project) = new ConnectServerDialog(project)

}
