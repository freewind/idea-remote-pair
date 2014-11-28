package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair.actions.forms.SendClientNameForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.settings.AppSettingsProperties
import com.thoughtworks.pli.intellij.remotepair.{ClientInfoEvent, InvokeLater, PublishEvents, RichProject}

class SendClientNameDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with AppSettingsProperties with PublishEvents with InvokeLater with LocalHostInfo with CurrentProjectHolder {

  init()

  override def createCenterPanel(): JComponent = {
    form.clientName = appProperties.clientName
    form.getMain
  }

  override def doOKAction(): Unit = {
    appProperties.clientName = form.clientName
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        publishEvent(ClientInfoEvent(localIp(), form.clientName))
      } catch {
        case e: Throwable => showError(e.toString)
      }
    }
  }

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  private object EarlyInitialization {
    val form = new SendClientNameForm
  }

  def form = EarlyInitialization.form

  override def getPreferredFocusedComponent: JComponent = {
    form.getTxtClientName
  }
}
