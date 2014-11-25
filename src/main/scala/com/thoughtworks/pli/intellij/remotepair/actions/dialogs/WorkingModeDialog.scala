package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.{ClientInfoHolder, CurrentProjectHolder}

class WorkingModeDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with InvokeLater with PublishEvents with ClientInfoHolder with CurrentProjectHolder {

  init()

  private object Early {
    val form = new WorkingModeForm
  }

  override def createCenterPanel(): JComponent = {
    form.getMainPanel
  }

  override def doOKAction(): Unit = invokeLater {
    close(DialogWrapper.OK_EXIT_CODE)

    if (form.isCaretSharingMode) {
      publishEvent(CaretSharingModeRequest)
    } else if (form.isParallelMode) {
      publishEvent(ParallelModeRequest)
    }
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  def form = Early.form

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

}
