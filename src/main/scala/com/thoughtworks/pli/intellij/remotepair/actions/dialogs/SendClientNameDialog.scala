package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.actions.forms.SendClientNameForm
import com.thoughtworks.pli.intellij.remotepair.settings.AppSettingsProperties
import com.thoughtworks.pli.intellij.remotepair.{ClientInfoEvent, InvokeLater, PublishEvents}
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo

class SendClientNameDialog(project: Project) extends DialogWrapper(project) with AppSettingsProperties with PublishEvents with InvokeLater with LocalHostInfo {

  init()

  override def createCenterPanel(): JComponent = {
    form.clientName = appProperties.clientName
    form.mainPanel
  }

  override def doOKAction(): Unit = {
    appProperties.clientName = form.clientName
    invokeLater {
      try {
        publishEvent(ClientInfoEvent(localIp(), form.clientName))
        close(DialogWrapper.OK_EXIT_CODE)
      } catch {
        case e: Throwable => showError(e.toString)
      }
    }
  }

  def showError(message: String) {
    Messages.showErrorDialog(project, message, "Error")
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  private object EarlyInitialization {
    val form = new SendClientNameForm
  }

  def form = EarlyInitialization.form

}
