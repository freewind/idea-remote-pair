package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.settings.{ProjectSettingsProperties, IdeaPluginServices}
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair._
import io.netty.util.concurrent.GenericFutureListener
import io.netty.channel.ChannelFuture
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.actions.forms.{ConnectServerForm, ConnectServerFormCreator}

trait ConnectServerDialogProvider {
  this: CurrentProjectHolder =>

  def createConnectServerDialog() = new ConnectServerDialog(currentProject)
}

class ConnectServerDialog(val currentProject: Project)
  extends DialogWrapper(currentProject)
  with ConnectServerFormCreator
  with IdeaPluginServices with LocalHostInfo
  with ProjectSettingsProperties with InvokeLater with CurrentProjectHolder {

  init()

  private object RunBeforeInitializing {
    val form: ConnectServerForm = createForm()
  }

  def form = RunBeforeInitializing.form

  override def createCenterPanel(): JComponent = {
    form.host = projectProperties.targetServerHost
    form.port = projectProperties.targetServerPort.toString
    form.mainPanel
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  override def doOKAction(): Unit = {
    storeInputValues()
    connectToServer()
  }

  def storeInputValues() = {
    projectProperties.targetServerHost = form.host
    projectProperties.targetServerPort = form.port.toInt
  }

  def connectToServer() {
    val (serverHost, serverPort) = (form.host, form.port.toInt)
    val component = currentProject.getComponent(classOf[RemotePairProjectComponent])
    invokeLater {
      component.connect(serverHost, serverPort).addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture) {
          if (f.isSuccess) {
            close(DialogWrapper.OK_EXIT_CODE)
          } else {
            showError(s"Can't connect to server $serverHost:$serverPort")
          }
        }
      })
    }
  }

  def showError(message: String) {
    Messages.showErrorDialog(currentProject, message, "Error")
  }

}


