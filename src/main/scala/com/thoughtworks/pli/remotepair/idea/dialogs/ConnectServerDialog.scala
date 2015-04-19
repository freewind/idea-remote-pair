package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.ui.ValidationInfo
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.{ProjectUrlInProjectStorage, ServerHostInProjectStorage, ServerPortInProjectStorage}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

object ConnectServerDialog {
  type Factory = () => ConnectServerDialog
}


class ConnectServerDialog(joinProjectDialogFactory: JoinProjectDialog.Factory, val invokeLater: InvokeLater, val pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: Client.Factory, serverHostInProjectStorage: ServerHostInProjectStorage, serverPortInProjectStorage: ServerPortInProjectStorage, val getProjectWindow: GetProjectWindow, channelHandlerHolder: ChannelHandlerHolder, publishEvent: PublishEvent, newUuid: NewUuid, projectUrlHelper: ProjectUrlHelper, getServerWatchingFiles: GetServerWatchingFiles, watchFilesDialogFactory: WatchFilesDialog.Factory, copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory, projectUrlInProjectStorage: ProjectUrlInProjectStorage) extends _ConnectServerDialog with JDialogSupport {

  private val newProjectName = newUuid()

  setSize(Size(400, 170))
  init()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    this.hostTextField.setText(serverHostInProjectStorage.load().getOrElse(""))
    this.portTextField.setText(serverPortInProjectStorage.load().getOrElse(DefaultValues.DefaultPort).toString)
  }

  onClick(createProjectButton) {
    validateInputs() match {
      case Some(e) => showErrorMessage(e.toString)
      case _ =>
        storeInputValues()
        connectToServer(new ServerAddress(this.getHost, this.getPort.toInt)) {
          publishCreateProjectEvent()
        }
    }
  }

  onClick(joinButton) {
    storeInputValues()
    val ProjectUrl(host, port, projectName) = projectUrlHelper.decode(joinUrlField.getText.trim)
    connectToServer(new ServerAddress(host, port)) {
      publishJoinProjectEvent(projectName)
    }
  }

  monitorReadEvent {
    case CreatedProjectEvent(projectName, clientName) => {
      generateProjectUrl(projectName)
      dispose()
      chooseWatchingFiles(showProjectUrlWhenClose = true)
    }
    case JoinedToProjectEvent(projectName, clientName) => {
      generateProjectUrl(projectName)
      dispose()
      chooseWatchingFiles(showProjectUrlWhenClose = false)
    }
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(getHost, getPort.toInt, projectName)
    projectUrlInProjectStorage.save(projectUrlHelper.encode(projectUrl))
  }

  private def chooseWatchingFiles(showProjectUrlWhenClose: Boolean): Unit = {
    this.dispose()
    if (getServerWatchingFiles().isEmpty) {
      val action = if (showProjectUrlWhenClose) {
        Some(() => copyProjectUrlDialogFactory().showOnCenter())
      } else {
        None
      }
      watchFilesDialogFactory(action).showOnCenter()
    }
  }

  private def showErrorMessage(msg: String): Unit = {
    this.message.setText(msg)
  }

  def storeInputValues() = {
    serverHostInProjectStorage.save(this.getHost)
    serverPortInProjectStorage.save(this.getPort.toInt)
  }

  def connectToServer(address: ServerAddress)(afterConnected: => Any) {
    invokeLater {
      try {
        val handler = myChannelHandlerFactory()
        channelHandlerHolder.put(Some(handler))

        clientFactory(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture) {
            if (f.isSuccess) invokeLater {
              afterConnected
            }
          }
        })

      } catch {
        case e: Throwable =>
          channelHandlerHolder.put(None)
          message.setText("Can't connect to server")
          message.setVisible(true)
      }
    }
  }

  private def publishCreateProjectEvent() = {
    publishEvent(new CreateProjectRequest(newProjectName, textUserNameInCreation.getText.trim))
  }

  def validateInputs(): Option[ValidationInfo] = (getHost, getPort) match {
    case (host, _) if host.isEmpty => Some(new ValidationInfo("server host should not be blank", hostTextField))
    case (_, port) if Try(port.toInt).isFailure => Some(new ValidationInfo("server port should be an integer", portTextField))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", portTextField))
    case _ => None
  }

  private def publishJoinProjectEvent(projectName: String): Unit = {
    publishEvent(new JoinProjectRequest(projectName, userNameInJoinField.getText.trim))
  }
}




