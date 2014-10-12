package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnActionEvent, AnAction}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.{RemotePairProjectComponent, InvokeLater}

class ConnectServerAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val serverUrl = askForServerInfo(project)
    connect(project, serverUrl)
  }

  private def askForServerInfo(project: Project) = {
    Messages.showInputDialog(project, "user@ip:port:project", "Input the server info", Messages.getQuestionIcon)
  }

  private def connect(project: Project, serverUrl: String) {
    val Array(userName, ip, port, targetProject) = serverUrl.split("[@:]")

    val component = project.getComponent(classOf[RemotePairProjectComponent])

    invokeLater {
      component.connect(ip, Integer.parseInt(port), targetProject, userName)
      Messages.showMessageDialog(project,
        s"Connected to $ip:$port", "Information",
        Messages.getInformationIcon)
    }
  }

}
