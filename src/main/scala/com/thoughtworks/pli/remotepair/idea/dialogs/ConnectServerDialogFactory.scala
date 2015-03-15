package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.ui.ValidationInfo
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.{ServerHostInProjectStorage, ServerPortInProjectStorage}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.util.Try

object ConnectServerDialogFactory {
  type ConnectServerDialog = ConnectServerDialogFactory#create
}

case class ConnectServerDialogFactory(newJoinProjectDialog: JoinProjectDialogFactory, invokeLater: InvokeLater, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandlerFactory, clientFactory: ClientFactory, serverHostInProjectStorage: ServerHostInProjectStorage, serverPortInProjectStorage: ServerPortInProjectStorage, getProjectWindow: GetProjectWindow, channelHandlerHolder: ChannelHandlerHolder) {
  factory =>

  case class create() extends _ConnectServerDialog with JDialogSupport {
    override def invokeLater = factory.invokeLater
    override def getProjectWindow = factory.getProjectWindow
    override def pairEventListeners = factory.pairEventListeners


    setSize(Size(400, 170))
    init()

    restoreInputValues()

    private def restoreInputValues(): Unit = {
      this.hostTextField.setText(serverHostInProjectStorage.load().getOrElse(""))
      this.portTextField.setText(serverPortInProjectStorage.load().getOrElse(DefaultValues.DefaultPort).toString)
    }

    onClick(connectButton) {
      validateInputs() match {
        case Some(e) =>
        case _ =>
          storeInputValues()
          connectToServer()
      }
    }

    onClick(closeButton) {
      dispose()
    }

    def storeInputValues() = {
      serverHostInProjectStorage.save(this.getHost)
      serverPortInProjectStorage.save(this.getPort.toInt)
    }

    def connectToServer() {
      val address = new ServerAddress(this.getHost, this.getPort.toInt)
      invokeLater {
        try {
          val handler = myChannelHandlerFactory.create()
          channelHandlerHolder.put(Some(handler))

          clientFactory.create(address).connect(handler).addListener(new GenericFutureListener[ChannelFuture] {
            override def operationComplete(f: ChannelFuture) {
              if (f.isSuccess) invokeLater {
                dispose()
                newJoinProjectDialog.create().showOnCenter()
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

    def validateInputs(): Option[ValidationInfo] = (getHost, getPort) match {
      case (host, _) if host.isEmpty => Some(new ValidationInfo("server host should not be blank", hostTextField))
      case (_, port) if Try(port.toInt).isFailure => Some(new ValidationInfo("server port should be an integer", portTextField))
      case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", portTextField))
      case _ => None
    }

  }

}



