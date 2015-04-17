package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.ui.ValidationInfo
import com.thoughtworks.pli.intellij.remotepair.protocol.{JoinProjectRequest, ProjectOperationFailed, JoinedToProjectEvent, CreateProjectRequest}
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.{ServerHostInProjectStorage, ServerPortInProjectStorage}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

object ConnectServerDialogFactory {
  type ConnectServerDialog = ConnectServerDialogFactory#create
}

case class ConnectServerDialogFactory(joinProjectDialogFactory: JoinProjectDialogFactory, invokeLater: InvokeLater, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandlerFactory, clientFactory: ClientFactory, serverHostInProjectStorage: ServerHostInProjectStorage, serverPortInProjectStorage: ServerPortInProjectStorage, getProjectWindow: GetProjectWindow, channelHandlerHolder: ChannelHandlerHolder, publishEvent: PublishEvent, newUuid: NewUuid, projectUrlHelper: ProjectUrlHelper, getServerWatchingFiles: GetServerWatchingFiles, watchFilesDialogFactory: WatchFilesDialogFactory) {
  factory =>

  case class create() extends _ConnectServerDialog with JDialogSupport {
    override def invokeLater = factory.invokeLater
    override def getProjectWindow = factory.getProjectWindow
    override def pairEventListeners = factory.pairEventListeners

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
      case JoinedToProjectEvent(projectName, clientName) => {
        val projectUrl = new ProjectUrl(getHost, getPort.toInt, newProjectName)
        println("#################################### project url: " + projectUrlHelper.encode(projectUrl))
        dispose()
        chooseWatchingFiles()
      }
      case ProjectOperationFailed(msg) => showErrorMessage(msg)
    }

    private def chooseWatchingFiles(): Unit = {
      this.dispose()
      if (getServerWatchingFiles().isEmpty) {
        watchFilesDialogFactory.create().showOnCenter()
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
          val handler = myChannelHandlerFactory.create()
          channelHandlerHolder.put(Some(handler))

          clientFactory.create(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
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

}



