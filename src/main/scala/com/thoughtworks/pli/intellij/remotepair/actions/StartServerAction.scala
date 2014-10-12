package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.server.Server
import java.net.InetAddress
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.intellij.remotepair.settings.{ObjectsHolder, AppSettingsProperties}

class StartServerAction extends AnAction with InvokeLater with LocalHostInfo with AppSettingsProperties with ObjectsHolder {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val port = appProperties.serverBindingPort
    startServer(project, port)
  }

  private def startServer(project: Project, port: Int) {
    invokeLater {
      (new Server).start(port)
      Messages.showMessageDialog(project,
        s"Server is started at: ${localIp()}:$port", "Information",
        Messages.getInformationIcon)
    }
  }

}


