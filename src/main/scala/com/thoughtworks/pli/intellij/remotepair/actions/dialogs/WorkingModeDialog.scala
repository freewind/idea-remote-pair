package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.ServerStatusHolder
import com.thoughtworks.pli.intellij.remotepair.{CaretSharingModeRequest, PublishEvents, InvokeLater}
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class WorkingModeDialog(project: Project) extends DialogWrapper(project) with ServerStatusHolder with InvokeLater with PublishEvents {

  init()

  private object Early {
    val form = new WorkingModeForm
  }

  override def createCenterPanel(): JComponent = {
//    form.setClientsInCaretSharingMode(caretSharingClients)
//    form.setClientsInFollowMode(followClients)
    form.mainPanel
  }

  override def doOKAction(): Unit = invokeLater {
//    form.getSelectedClientNameInCaretSharingMode match {
//      case Some(name) => publishEvent(CaretSharingModeRequest(name))
//      case _ =>
//    }
  }

  override def doValidate(): ValidationInfo = super.doValidate()

  def form = Early.form

  def showError(message: String) {
    Messages.showErrorDialog(project, message, "Error")
  }
}
