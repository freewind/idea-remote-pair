package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ConnectServerForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.settings.{IdeaPluginServices, ProjectSettingsProperties}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class ConnectServerDialog(project: Project)
  extends DialogWrapper(project)
  with IdeaPluginServices with LocalHostInfo
  with ProjectSettingsProperties with InvokeLater with CurrentProjectHolder {

  override val currentProject = new RichProject(project)

  init()

  private object RunBeforeInitializing {
    val form: ConnectServerForm = new ConnectServerForm()
  }

  def form = RunBeforeInitializing.form

  override def createCenterPanel(): JComponent = {
    form.host = projectProperties.targetServerHost
    form.port = projectProperties.targetServerPort.toString
    form.getMain
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  override def doOKAction(): Unit = {
    storeInputValues()
    close(DialogWrapper.OK_EXIT_CODE)
    connectToServer()
  }

  def storeInputValues() = {
    projectProperties.targetServerHost = form.host
    projectProperties.targetServerPort = form.port.toInt
  }

  def connectToServer() {
    val (serverHost, serverPort) = (form.host, form.port.toInt)
    val component = currentProject.getComponent[RemotePairProjectComponent]
    invokeLater {
      component.connect(serverHost, serverPort).addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture) {
          if (!f.isSuccess) {
            invokeLater(showError(s"Can't connect to server $serverHost:$serverPort"))
          }
        }
      })
    }
  }

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

}


