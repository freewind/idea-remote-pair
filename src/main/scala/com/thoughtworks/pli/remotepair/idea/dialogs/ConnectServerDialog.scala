package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JDialog

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models._
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.dialogs.BaseVirtualDialog
import com.thoughtworks.pli.remotepair.core.{DefaultValues, MyUtils}
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.language.reflectiveCalls
import scala.util.Try

case class ConnectServerDialog(currentProject: MyProject, myProjectStorage: MyProjectStorage, myIde: MyIde, myUtils: MyUtils, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, dialogFactories: DialogFactories, myClient: MyClient)
  extends _ConnectServerDialog with JDialogSupport with BaseVirtualDialog {

  setSize(Size(400, 220))
  init()

  private val newProjectName = myUtils.newUuid()

  def init(): Unit = {
    _message.visible = false
    _hostTextField.requestFocus()
    restoreInputValues()

    monitorReadEvent {
      case CreatedProjectEvent(projectName, clientName) => {
        generateProjectUrl(projectName)
        this.dispose()
        if (myClient.serverWatchingFiles.isEmpty) {
          chooseWatchingFiles(showProjectUrlWhenClose = true)
        }
      }
      case JoinedToProjectEvent(projectName, clientName) => {
        myClient.setReadonlyMode(_readonlyCheckBox.isSelected)
        generateProjectUrl(projectName)
        this.dispose()
        if (myClient.serverWatchingFiles.isEmpty) {
          chooseWatchingFiles(showProjectUrlWhenClose = false)
        } else {
          dialogFactories.createSyncFilesForSlaveDialog.showOnCenter()
        }
      }
      case ProjectOperationFailed(msg) => showErrorMessage(msg)
    }

    _createProjectButton.onClick {
      myIde.invokeLater {
        validateInputsForCreatingProject() match {
          case Some(e) => showErrorMessage(e.toString)
          case _ =>
            storeInputValues()
            connectToServer(new ServerAddress(_hostTextField.text.trim, _portTextField.text.trim.toInt)) {
              publishCreateProjectEvent()
            }
        }
      }
    }

    _joinProjectButton.onClick {
      myIde.invokeLater {
        validateInputsForJoiningProject() match {
          case Some(e) => showErrorMessage(e)
          case _ =>
            storeInputValues()
            val ProjectUrl(host, port, projectName) = ProjectUrl.decode(_joinUrlField.text.trim)
            connectToServer(new ServerAddress(host, port)) {
              publishJoinProjectEvent(projectName)
            }
        }
      }
    }
  }

  private def restoreInputValues(): Unit = {
    _hostTextField.text = myProjectStorage.serverHost.getOrElse("")
    _portTextField.text = myProjectStorage.serverPort.getOrElse(DefaultValues.DefaultServerPort).toString
    _joinUrlField.text = myProjectStorage.projectUrl.getOrElse("")
    _clientNameInCreationField.text = myProjectStorage.clientName.getOrElse("")
    _clientNameInJoinField.text = myProjectStorage.clientName.getOrElse("")
  }

  def validateInputsForCreatingProject(): Option[String] = {
    val (host, port, clientName) = (_hostTextField.text.trim, _portTextField.text.trim, _clientNameInCreationField.text.trim)
    if (host.isEmpty) Some("server host should not be blank")
    else if (Try(port.toInt).isFailure) Some("server port should be an integer")
    else if (port.toInt <= 0) Some("server port should > 0")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else None
  }

  private def showErrorMessage(msg: String): Unit = {
    _message.text = msg
    _message.visible = true
  }

  def storeInputValues() = {
    myProjectStorage.serverHost = _hostTextField.text.trim
    myProjectStorage.serverPort = _portTextField.text.trim.toInt
    myProjectStorage.projectUrl = _joinUrlField.text.trim
    myProjectStorage.clientName = {
      val names = Seq(_clientNameInCreationField.text, _clientNameInJoinField.text).map(_.trim).filterNot(_.isEmpty)
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
    myClient.publishEvent(new CreateProjectRequest(newProjectName, _clientNameInCreationField.text.trim))
  }

  def validateInputsForJoiningProject(): Option[String] = {
    val (joinUrl, clientName) = (_joinUrlField.text.trim, _clientNameInJoinField.text.trim)
    if (joinUrl.isEmpty) Some("join url should not be blank")
    else if (clientName.isEmpty) Some("my name in project should not be blank")
    else if (Try(ProjectUrl.decode(_joinUrlField.text.trim)).isFailure) Some("join url is invalid")
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
    myClient.publishEvent(new JoinProjectRequest(projectName, _clientNameInJoinField.text.trim))
  }

  def generateProjectUrl(projectName: String) {
    val projectUrl = new ProjectUrl(_hostTextField.text.trim, _portTextField.text.trim.toInt, projectName)
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





