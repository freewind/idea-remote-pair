package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnActionEvent, AnAction}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialogProvider
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class ConnectServerAction extends AnAction with InvokeLater with ConnectServerDialogProvider with CurrentProjectHolder {

  private var project: Project = _

  override def currentProject: Project = project

  def actionPerformed(event: AnActionEvent) {
    project = event.getData(CommonDataKeys.PROJECT)
    val dialog = createConnectServerDialog()
    dialog.show()
  }

}
