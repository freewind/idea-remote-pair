package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, DialogWrapper}
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.settings.{ProjectSettingsProperties, IdeaPluginServices, AppSettingsProperties}
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair._
import io.netty.util.concurrent.GenericFutureListener
import io.netty.channel.ChannelFuture
import com.thoughtworks.pli.intellij.remotepair.ClientInfoEvent

trait ConnectServerDialogProvider extends IdeaPluginServices with LocalHostInfo with AppSettingsProperties
with ProjectSettingsProperties with InvokeLater {
  this: CurrentProjectHolder =>

  def createConnectServerDialog(): DialogWrapper = new ConnectServerDialogWrapper

  class ConnectServerDialogWrapper extends DialogWrapper(currentProject) {

    init()

    var form: ConnectServerForm = _

    override def createCenterPanel(): JComponent = {
      form = new ConnectServerForm
      form.init(projectProperties.targetServerHost, projectProperties.targetServerPort, appProperties.clientName)
      form.getMain
    }

    override def doOKAction(): Unit = {
      val (serverHost, serverPort, clientName) = (form.getIp, form.getPort, form.getUsername)
      projectProperties.targetServerHost = serverHost
      projectProperties.targetServerPort = serverPort
      appProperties.clientName = clientName

      val component = currentProject.getComponent(classOf[RemotePairProjectComponent])
      invokeLater {
        component.connect(serverHost, serverPort).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture) {
            if (f.isSuccess) {
              f.channel().writeAndFlush(ClientInfoEvent(localIp(), clientName).toMessage)
            } else {
              showError(s"Can't connect to server $serverHost:$serverPort")
            }
          }
        })
      }
    }

    private def showError(message: String) {
      Messages.showMessageDialog(currentProject, message, "Error", Messages.getErrorIcon)
    }
  }

}

