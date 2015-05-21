package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.{Client, GetServerWatchingFiles, MyChannelHandler, PublishEvent}
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.idea.DefaultValues
import com.thoughtworks.pli.remotepair.idea.idea.GetProjectWindow
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.settings._
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

object ConnectServerDialog {
  type Factory = () => ConnectServerDialog
}

class ConnectServerDialog(joinProjectDialogFactory: JoinProjectDialog.Factory, val myPlatform: MyPlatform, val pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: Client.Factory, serverHostInProjectStorage: ServerHostInProjectStorage, serverPortInProjectStorage: ServerPortInProjectStorage, clientNameInCreationInProjectStorage: ClientNameInCreationInProjectStorage, clientNameInJoinInProjectStorage: ClientNameInJoinInProjectStorage, val getProjectWindow: GetProjectWindow, channelHandlerHolder: ChannelHandlerHolder, publishEvent: PublishEvent, newUuid: NewUuid, getServerWatchingFiles: GetServerWatchingFiles, watchFilesDialogFactory: WatchFilesDialog.Factory, copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory, projectUrlInProjectStorage: ProjectUrlInProjectStorage, setReadonlyMode: SetReadonlyMode, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory)
  extends _ConnectServerDialog with JDialogSupport {

  private val newProjectName = newUuid()

  setSize(Size(400, 220))
  init()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    this.hostTextField.setText(serverHostInProjectStorage.load().getOrElse(""))
    this.portTextField.setText(serverPortInProjectStorage.load().getOrElse(DefaultValues.DefaultPort).toString)
    this.clientNameInCreationField.setText(clientNameInCreationInProjectStorage.load().getOrElse(""))
    this.joinUrlField.setText(projectUrlInProjectStorage.load().getOrElse(""))
    this.clientNameInJoinField.setText(clientNameInJoinInProjectStorage.load().getOrElse(""))
  }

  onClick(createProjectButton) {
    validateInputsForCreatingProject() match {
      case Some(e) => showErrorMessage(e.toString)
      case _ =>
        storeInputValues()
        connectToServer(new ServerAddress(hostTextField.getText.trim, portTextField.getText.trim.toInt)) {
          publishCreateProjectEvent()
        }
    }
  }

  onClick(joinButton) {
    validateInputsForJoiningProject() match {
      case Some(e) => showErrorMessage(e.toString)
      case _ =>
        storeInputValues()
        val ProjectUrl(host, port, projectName) = ProjectUrl.decode(joinUrlField.getText.trim)
        connectToServer(new ServerAddress(host, port)) {
          publishJoinProjectEvent(projectName)
        }
    }
  }

  monitorReadEvent {
    case CreatedProjectEvent(projectName, clientName) => {
      generateProjectUrl(projectName)
      dispose()
      if (getServerWatchingFiles().isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = true)
      }
    }
    case JoinedToProjectEvent(projectName, clientName) => {
      setReadonlyMode(readonlyCheckBox.isSelected)
      generateProjectUrl(projectName)
      dispose()
      if (getServerWatchingFiles().isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = false)
      } else {
        syncFilesForSlaveDialogFactory().showOnCenter()
      }
    }
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(hostTextField.getText.trim, portTextField.getText.trim.toInt, projectName)
    projectUrlInProjectStorage.save(ProjectUrl.encode(projectUrl))
  }

  private def chooseWatchingFiles(showProjectUrlWhenClose: Boolean): Unit = {
    val action = if (showProjectUrlWhenClose) {
      Some(() => copyProjectUrlDialogFactory().showOnCenter())
    } else {
      None
    }
    watchFilesDialogFactory(action).showOnCenter()
  }

  private def showErrorMessage(msg: String): Unit = {
    this.message.setText(msg)
    this.message.setVisible(true)
  }

  def storeInputValues() = {
    serverHostInProjectStorage.save(hostTextField.getText.trim)
    serverPortInProjectStorage.save(portTextField.getText.trim.toInt)
    clientNameInCreationInProjectStorage.save(clientNameInCreationField.getText.trim)
    projectUrlInProjectStorage.save(joinUrlField.getText.trim)
    clientNameInJoinInProjectStorage.save(clientNameInJoinField.getText.trim)
  }

  def connectToServer(address: ServerAddress)(afterConnected: => Any) {
    myPlatform.invokeLater {
      try {
        val handler = myChannelHandlerFactory()
        channelHandlerHolder.put(Some(handler))

        clientFactory(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture) {
            if (f.isSuccess) myPlatform.invokeLater {
              afterConnected
            }
          }
        })

      } catch {
        case e: Throwable =>
          channelHandlerHolder.put(None)
          showErrorMessage("Can't connect to server")
      }
    }
  }

  private def publishCreateProjectEvent() = {
    publishEvent(new CreateProjectRequest(newProjectName, clientNameInCreationField.getText.trim))
  }

  def validateInputsForCreatingProject(): Option[String] = {
    val (host, port, clientName) = (hostTextField.getText.trim, portTextField.getText.trim, clientNameInCreationField.getText.trim)
    if (host.isEmpty) Some("server host should not be blank")
    else if (Try(port.toInt).isFailure) Some("server port should be an integer")
    else if (port.toInt <= 0) Some("server port should > 0")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else None
  }

  def validateInputsForJoiningProject(): Option[String] = {
    val (joinUrl, clientName) = (joinUrlField.getText.trim, clientNameInJoinField.getText.trim)
    if (joinUrl.isEmpty) Some("join url should not be blank")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else if (Try(ProjectUrl.decode(joinUrlField.getText.trim)).isFailure) Some("join url is invalid")
    else None
  }

  private def publishJoinProjectEvent(projectName: String): Unit = {
    publishEvent(new JoinProjectRequest(projectName, clientNameInJoinField.getText.trim))
  }
}




