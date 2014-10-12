package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.server.Server
import java.net.InetAddress
import com.thoughtworks.pli.intellij.remotepair.InvokeLater

class StartServerAction extends AnAction with InvokeLater with LocalIpGetter {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val portStr = inputPort(project)
    try {
      val port = Integer.parseInt(portStr)
      startServer(project, port)
    } catch {
      case e: NumberFormatException => showErrorMessage(project, "Invalid port: " + portStr)
      case e: Throwable => showErrorMessage(project, e.getMessage)
    }
  }

  private def showErrorMessage(project: Project, message: String) = {
    Messages.showMessageDialog(project, message, "Error", Messages.getErrorIcon)
  }

  private def inputPort(project: Project) = {
    Messages.showInputDialog(project, s"${localIp()}:port", "Server will binding a port", Messages.getQuestionIcon)
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

trait LocalIpGetter {
  def localIp() = InetAddress.getLocalHost.getHostAddress
}
