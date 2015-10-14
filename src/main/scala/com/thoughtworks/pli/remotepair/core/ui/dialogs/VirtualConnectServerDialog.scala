package com.thoughtworks.pli.remotepair.core.ui.dialogs

import akka.actor.Actor
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.{MyChannelHandler, MyClient, NettyClient, ServerAddress}
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualCheckBox, VirtualInputField, VirtualLabel}
import com.thoughtworks.pli.remotepair.core.{DefaultValues, MyUtils}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

trait VirtualConnectServerDialog extends BaseVirtualDialog {
  def myUtils: MyUtils
  def myProjectStorage: MyProjectStorage
  def myIde: MyIde
  def myChannelHandlerFactory: MyChannelHandler.Factory
  def myClient: MyClient
  def clientFactory: NettyClient.Factory
  def dialogFactories: DialogFactories

  private val newProjectName = myUtils.newUuid()

  val serverHostField: VirtualInputField
  val serverPortField: VirtualInputField
  val clientNameInCreationField: VirtualInputField
  val clientNameInJoinField: VirtualInputField
  val projectUrlField: VirtualInputField
  val messageLabel: VirtualLabel
  val createProjectButton: VirtualButton
  val joinProjectButton: VirtualButton
  val readonlyCheckBox: VirtualCheckBox

  def init(): Unit = {
    messageLabel.visible = false
    serverHostField.requestFocus()
    restoreInputValues()

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
          dialogFactories.createSyncFilesForSlaveDialog.showOnCenter()
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
          case Some(e) => showErrorMessage(e)
          case _ =>
            storeInputValues()
            val ProjectUrl(host, port, projectName) = ProjectUrl.decode(projectUrlField.text.trim)
            connectToServer(new ServerAddress(host, port)) {
              publishJoinProjectEvent(projectName)
            }
        }
      }
    }
  }

  private def restoreInputValues(): Unit = {
    serverHostField.text = myProjectStorage.serverHost.getOrElse("")
    serverPortField.text = myProjectStorage.serverPort.getOrElse(DefaultValues.DefaultServerPort).toString
    projectUrlField.text = myProjectStorage.projectUrl.getOrElse("")
    clientNameInCreationField.text = myProjectStorage.clientName.getOrElse("")
    clientNameInJoinField.text = myProjectStorage.clientName.getOrElse("")
  }

  def validateInputsForCreatingProject(): Option[String] = {
    val (host, port, clientName) = (serverHostField.text.trim, serverPortField.text.trim, clientNameInCreationField.text.trim)
    if (host.isEmpty) Some("server host should not be blank")
    else if (Try(port.toInt).isFailure) Some("server port should be an integer")
    else if (port.toInt <= 0) Some("server port should > 0")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else None
  }

  private def showErrorMessage(msg: String): Unit = {
    this.messageLabel.text = msg
    this.messageLabel.visible = true
  }

  def storeInputValues() = {
    myProjectStorage.serverHost = serverHostField.text.trim
    myProjectStorage.serverPort = serverPortField.text.trim.toInt
    myProjectStorage.projectUrl = projectUrlField.text.trim
    myProjectStorage.clientName = {
      val names = Seq(clientNameInCreationField.text, clientNameInJoinField.text).map(_.trim).filterNot(_.isEmpty)
      names.find(_ != myProjectStorage.clientName).orElse(names.headOption).getOrElse("")
    }
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
    myClient.publishEvent(new CreateProjectRequest(newProjectName, clientNameInCreationField.text.trim))
  }

  def validateInputsForJoiningProject(): Option[String] = {
    val (joinUrl, clientName) = (projectUrlField.text.trim, clientNameInJoinField.text.trim)
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
    myClient.publishEvent(new JoinProjectRequest(projectName, clientNameInJoinField.text.trim))
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(serverHostField.text.trim, serverPortField.text.trim.toInt, projectName)
    myProjectStorage.projectUrl = ProjectUrl.encode(projectUrl)
  }

  private def chooseWatchingFiles(showProjectUrlWhenClose: Boolean): Unit = {
    val action = if (showProjectUrlWhenClose) {
      Some(() => dialogFactories.createCopyProjectUrlDialog.showOnCenter())
    } else {
      None
    }
    dialogFactories.createWatchFilesDialog(action).showOnCenter()
  }

}
