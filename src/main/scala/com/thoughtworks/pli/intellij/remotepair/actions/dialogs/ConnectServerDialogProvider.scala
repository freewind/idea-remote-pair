package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.settings.{ProjectSettingsProperties, IdeaPluginServices, AppSettingsProperties}
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair._
import io.netty.util.concurrent.GenericFutureListener
import io.netty.channel.ChannelFuture
import com.intellij.openapi.project.Project
import scala.util.Try
import javax.swing.event.{DocumentEvent, DocumentListener}
import com.thoughtworks.pli.intellij.remotepair.client.{CurrentProjectHolder, InitializingProcessCreator}

trait ConnectServerDialogProvider {
  this: CurrentProjectHolder =>

  def createConnectServerDialog() = new ConnectServerDialogWrapper(currentProject)

}

trait ConnectServerFormCreator {
  def createForm() = new ConnectServerForm
}

class ConnectServerDialogWrapper(val currentProject: Project)
  extends DialogWrapper(currentProject)
  with ConnectServerFormCreator
  with InitializingProcessCreator
  with IdeaPluginServices with LocalHostInfo with AppSettingsProperties
  with ProjectSettingsProperties with InvokeLater with CurrentProjectHolder {

  init()

  private object RunBeforeInitializing {
    val form: ConnectServerForm = createForm()
  }

  def form = RunBeforeInitializing.form

  override def createCenterPanel(): JComponent = {
    setOkButtonStatus()
    form.init(projectProperties.targetServerHost, projectProperties.targetServerPort, appProperties.clientName)
    form.getMainPanel
  }

  def setOkButtonStatus() {
    val listener = new DocumentListener {
      override def insertUpdate(e: DocumentEvent): Unit = doit()
      override def changedUpdate(e: DocumentEvent): Unit = doit()
      override def removeUpdate(e: DocumentEvent): Unit = doit()
      private def doit() {
        setOKActionEnabled(doValidate() == null)
      }
    }
    form.getClientNameField.getDocument.addDocumentListener(listener)
    form.getServerHostField.getDocument.addDocumentListener(listener)
    form.getServerPortField.getDocument.addDocumentListener(listener)
  }
  override def doValidate(): ValidationInfo = {
    def isInteger(s: String) = Try(s.toInt).isSuccess
    (form.getHost, form.getPort, form.getClientName) match {
      case (host, _, _) if host.trim.isEmpty => new ValidationInfo("server host should not be blank", form.getServerHostField)
      case (_, port, _) if !isInteger(port) => new ValidationInfo("server port should be an integer", form.getServerPortField)
      case (_, port, _) if port.toInt <= 0 => new ValidationInfo("server port should > 0", form.getServerPortField)
      case (_, _, name) if name.trim.isEmpty => new ValidationInfo("Client name should not be blank", form.getClientNameField)
      case _ => null
    }
  }
  override def doOKAction(): Unit = {
    storeInputValues()
    connectToServer()
  }

  def storeInputValues() = {
    projectProperties.targetServerHost = form.getHost
    projectProperties.targetServerPort = form.getPort.toInt
    appProperties.clientName = form.getClientName
  }

  def connectToServer() {
    val (serverHost, serverPort) = (form.getHost, form.getPort.toInt)
    val component = currentProject.getComponent(classOf[RemotePairProjectComponent])
    invokeLater {
      component.connect(serverHost, serverPort).addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture) {
          if (f.isSuccess) {
            createInitializingProcess().start()
            close(DialogWrapper.OK_EXIT_CODE)
          } else {
            showError(s"Can't connect to server $serverHost:$serverPort")
          }
        }
      })
    }
  }

  def showError(message: String) {
    Messages.showMessageDialog(currentProject, message, "Error", Messages.getErrorIcon)
  }

}


