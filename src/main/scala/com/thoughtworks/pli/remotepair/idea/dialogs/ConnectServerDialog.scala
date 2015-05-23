package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.idea.DefaultValues
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

object ConnectServerDialog {
  type Factory = () => ConnectServerDialog
}

class ConnectServerDialog(val currentProject: IdeaProjectImpl, myProjectStorage: MyProjectStorage, val myIde: MyIde, val pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, newUuid: NewUuid, watchFilesDialogFactory: WatchFilesDialog.Factory, copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory, myClient: MyClient)
  extends _ConnectServerDialog with JDialogSupport {

  private val newProjectName = newUuid()

  setSize(Size(400, 220))
  init()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    this.hostTextField.setText(myProjectStorage.serverHost.getOrElse(""))
    this.portTextField.setText(myProjectStorage.serverPort.getOrElse(DefaultValues.DefaultPort).toString)
    this.joinUrlField.setText(myProjectStorage.projectUrl.getOrElse(""))
    this.clientNameInCreationField.setText(myProjectStorage.clientName.getOrElse(""))
    this.clientNameInJoinField.setText(myProjectStorage.clientName.getOrElse(""))
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
      if (myClient.serverWatchingFiles.isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = true)
      }
    }
    case JoinedToProjectEvent(projectName, clientName) => {
      myClient.setReadonlyMode(readonlyCheckBox.isSelected)
      generateProjectUrl(projectName)
      dispose()
      if (myClient.serverWatchingFiles.isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = false)
      } else {
        syncFilesForSlaveDialogFactory().showOnCenter()
      }
    }
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(hostTextField.getText.trim, portTextField.getText.trim.toInt, projectName)
    myProjectStorage.projectUrl = ProjectUrl.encode(projectUrl)
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
    myProjectStorage.serverHost = hostTextField.getText.trim
    myProjectStorage.serverPort = portTextField.getText.trim.toInt
    myProjectStorage.projectUrl = joinUrlField.getText.trim
    myProjectStorage.clientName = Seq(clientNameInCreationField.getText, clientNameInJoinField.getText)
      .map(_.trim).filterNot(_.isEmpty).headOption.getOrElse("")
  }

  def connectToServer(address: ServerAddress)(afterConnected: => Any) {
    myIde.invokeLater {
      try {
        val handler = myChannelHandlerFactory()
        myClient.channelHandlerHolder.set(Some(handler))

        clientFactory(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture) {
            if (f.isSuccess) myIde.invokeLater {
              afterConnected
            }
          }
        })

      } catch {
        case e: Throwable =>
          myClient.channelHandlerHolder.set(None)
          showErrorMessage("Can't connect to server")
      }
    }
  }

  private def publishCreateProjectEvent() = {
    myClient.publishEvent(new CreateProjectRequest(newProjectName, clientNameInCreationField.getText.trim))
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
    myClient.publishEvent(new JoinProjectRequest(projectName, clientNameInJoinField.getText.trim))
  }

  case class ProjectUrl(host: String, port: Int, projectCode: String)

  object ProjectUrl {
    def encode(projectUrl: ProjectUrl): String = {
      val ProjectUrl(host, port, projectCode) = projectUrl
      s"$host:$port:$projectCode"
    }
    def decode(url: String): ProjectUrl = url.split(":") match {
      case Array(host, port, projectCode) => ProjectUrl(host, port.toInt, projectCode)
    }
  }

}




