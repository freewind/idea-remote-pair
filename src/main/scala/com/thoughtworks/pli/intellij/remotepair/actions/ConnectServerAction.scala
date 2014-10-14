package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnActionEvent, AnAction}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.{CurrentProjectHolder, InvokeLater}
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialogProvider

class ConnectServerAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val provider = new ConnectServerDialogProvider with CurrentProjectHolder {
      override def currentProject: Project = project
    }

    val dialog = provider.createConnectServerDialog()
    dialog.show()
  }

  private def askForServerInfo(project: Project) = {
    Messages.showInputDialog(project, "user@ip:port:project", "Input the server info", Messages.getQuestionIcon)
  }

  private def connect(project: Project, serverUrl: String) {
    val Array(userName, ip, port, targetProject) = serverUrl.split("[@:]")


  }

}
