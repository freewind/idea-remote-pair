package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.{MyChannelHandler, MyClient, NettyClient, ServerAddress}
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualCheckBox, VirtualInputField, VirtualLabel}
import com.thoughtworks.pli.remotepair.idea.DefaultValues
import com.thoughtworks.pli.remotepair.idea.dialogs.{CopyProjectUrlDialog, SyncFilesForSlaveDialog, WatchFilesDialog}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

trait VirtualConnectServerDialog extends MonitorEvents {
  def myUtils: MyUtils
  def myProjectStorage: MyProjectStorage
  def myIde: MyIde
  def myChannelHandlerFactory: MyChannelHandler.Factory
  def myClient: MyClient
  def clientFactory: NettyClient.Factory
  def syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory
  def watchFilesDialogFactory: WatchFilesDialog.Factory
  def copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory

  private val newProjectName = myUtils.newUuid()

  val serverHostField: VirtualInputField
  val serverPortField: VirtualInputField
  val clientNameField: VirtualInputField
  val projectUrlField: VirtualInputField
  val messageLabel: VirtualLabel
  val createProjectButton: VirtualButton
  val joinProjectButton: VirtualButton
  val readonlyCheckBox: VirtualCheckBox

  messageLabel.visible_=(false)
  serverHostField.requestFocus()

  restoreInputValues()

  private def restoreInputValues(): Unit = {
    serverHostField.text = myProjectStorage.serverHost.getOrElse("")
    serverPortField.text = myProjectStorage.serverPort.getOrElse(DefaultValues.DefaultPort).toString
    projectUrlField.text = myProjectStorage.projectUrl.getOrElse("")
    clientNameField.text = myProjectStorage.clientName.getOrElse("")
  }

  monitorReadEvent {
    case CreatedProjectEvent(projectName, clientName) => {
      generateProjectUrl(projectName)
      dialog.dispose()
      if (myClient.serverWatchingFiles.isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = true)
      }
    }
    case JoinedToProjectEvent(projectName, clientName) => {
      myClient.setReadonlyMode(readonlyCheckBox.isSelected)
      generateProjectUrl(projectName)
      dialog.dispose()
      if (myClient.serverWatchingFiles.isEmpty) {
        chooseWatchingFiles(showProjectUrlWhenClose = false)
      } else {
        syncFilesForSlaveDialogFactory().showOnCenter()
      }
    }
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }

  createProjectButton.onClick {
    myIde.invokeLater {
      validateInputsForCreatingProject() match {
        case Some(e) => showErrorMessage(e.toString)
        case _ =>
          storeInputValues()
          connectToServer(new ServerAddress(serverHostField.text.trim, serverPortField.text.trim.toInt)) {
            publishCreateProjectEvent()
          }
      }
    }
  }

  joinProjectButton.onClick {
    myIde.invokeLater {
      validateInputsForJoiningProject() match {
        case Some(e) => showErrorMessage(e.toString)
        case _ =>
          storeInputValues()
          val ProjectUrl(host, port, projectName) = ProjectUrl.decode(projectUrlField.text.trim)
          connectToServer(new ServerAddress(host, port)) {
            publishJoinProjectEvent(projectName)
          }
      }
    }
  }

  def validateInputsForCreatingProject(): Option[String] = {
    val (host, port, clientName) = (serverHostField.text.trim, serverPortField.text.trim, clientNameField.text.trim)
    if (host.isEmpty) Some("server host should not be blank")
    else if (Try(port.toInt).isFailure) Some("server port should be an integer")
    else if (port.toInt <= 0) Some("server port should > 0")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else None
  }

  private def showErrorMessage(msg: String): Unit = {
    this.messageLabel.text_=(msg)
    this.messageLabel.visible_=(true)
  }

  def storeInputValues() = {
    myProjectStorage.serverHost = serverHostField.text.trim
    myProjectStorage.serverPort = serverPortField.text.trim.toInt
    myProjectStorage.projectUrl = projectUrlField.text.trim
    myProjectStorage.clientName = clientNameField.text.trim
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
    myClient.publishEvent(new CreateProjectRequest(newProjectName, clientNameField.text.trim))
  }

  def validateInputsForJoiningProject(): Option[String] = {
    val (joinUrl, clientName) = (projectUrlField.text.trim, clientNameField.text.trim)
    if (joinUrl.isEmpty) Some("join url should not be blank")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else if (Try(ProjectUrl.decode(projectUrlField.text.trim)).isFailure) Some("join url is invalid")
    else None
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

  private def publishJoinProjectEvent(projectName: String): Unit = {
    myClient.publishEvent(new JoinProjectRequest(projectName, clientNameField.text.trim))
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(serverHostField.text.trim, serverPortField.text.trim.toInt, projectName)
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

}
