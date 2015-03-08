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

  setSize(Size(400, 170))
  init()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    this.hostTextField.setText(projectProperties.targetServerHost)
    this.portTextField.setText(projectProperties.targetServerPort.toString)
  }

  clickOn(connectButton) {
    validateInputs() match {
      case Some(e) =>
      case _ =>
        storeInputValues()
        connectToServer()
    }
  }

  clickOn(closeButton) {
    dispose()
  }

  def storeInputValues() = {
    projectProperties.targetServerHost = this.getHost
    projectProperties.targetServerPort = this.getPort.toInt
  }

  def connectToServer() {
    val address = new ServerAddress(this.getHost, this.getPort.toInt)
    invokeLater {
      try {
        val handler = new MyChannelHandler(currentProject)
        currentProject.eventHandler = Some(handler)

        new Client(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture) {
            if (f.isSuccess) invokeLater {
              dispose()
              new JoinProjectDialog(currentProject).showOnCenter()
            }
          }
        })

      } catch {
        case e: Throwable =>
          currentProject.eventHandler = None
          message.setText("Can't connect to server")
          message.setVisible(true)
      }
    }
  }

  def validateInputs(): Option[ValidationInfo] = (getHost, getPort) match {
    case (host, _) if host.isEmpty => Some(new ValidationInfo("server host should not be blank", hostTextField))
    case (_, port) if Try(port.toInt).isFailure => Some(new ValidationInfo("server port should be an integer", portTextField))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", portTextField))
    case _ => None
  }

}



