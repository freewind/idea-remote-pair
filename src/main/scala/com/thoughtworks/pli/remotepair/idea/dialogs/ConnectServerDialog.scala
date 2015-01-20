package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.thoughtworks.pli.remotepair.idea.actions.LocalHostInfo
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.{IdeaPluginServices, ProjectSettingsProperties}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

class ConnectServerDialog(project: Project)
  extends _ConnectServerDialog
  with IdeaPluginServices with LocalHostInfo
  with ProjectSettingsProperties with InvokeLater with CurrentProjectHolder with PublishVersionedDocumentEvents with EventHandler with JDialogSupport {

  override val currentProject = Projects.init(project)

  init()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    this.txtHost.setText(projectProperties.targetServerHost)
    this.txtPort.setText(projectProperties.targetServerPort.toString)
  }

  clickOn(connectButton) {
    validateInputs() match {
      case Some(e) =>
      case _ =>
        storeInputValues()
        connectToServer()
    }
  }

  def storeInputValues() = {
    projectProperties.targetServerHost = this.getHost
    projectProperties.targetServerPort = this.getPort.toInt
  }

  def connectToServer() {
    val address = new ServerAddress(this.getHost, this.getPort.toInt)
    invokeLater {
      new Client(currentProject, address).connect(currentProject.eventHandler).addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture) {
          if (!f.isSuccess) {
            invokeLater(currentProject.showErrorDialog(message = s"Can't connect to server $address"))
          }
        }
      })
    }
  }

  def validateInputs(): Option[ValidationInfo] = (getHost, getPort) match {
    case (host, _) if host.isEmpty => Some(new ValidationInfo("server host should not be blank", txtHost))
    case (_, port) if Try(port.toInt).isFailure => Some(new ValidationInfo("server port should be an integer", txtPort))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", txtPort))
    case _ => None
  }

}



