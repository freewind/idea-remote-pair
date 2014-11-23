package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair.{FollowModeRequest, _}
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.{ClientInfoHolder, CurrentProjectHolder, ServerStatusHolder}

class WorkingModeDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with ServerStatusHolder with InvokeLater with PublishEvents with ClientInfoHolder with CurrentProjectHolder {

  init()

  private object Early {
    val form = new WorkingModeForm
  }

  override def createCenterPanel(): JComponent = {
    form.setClientsInCaretSharingMode(caretSharingClients)
    form.setClientsInFollowMode(followClients)
    form.getMainPanel
  }

  override def doOKAction(): Unit = invokeLater {
    close(DialogWrapper.OK_EXIT_CODE)

    if (form.isCaretSharingMode) {
      publishEvent(CaretSharingModeRequest)
    } else if (form.isParallelMode) {
      publishEvent(ParallelModeRequest)
    } else {
      form.getSelectedClientNameInFollowMode match {
        case Some(name) => publishEvent(FollowModeRequest(name))
        case _ => // should not happen because of the validation
      }
    }
  }

  override def doValidate(): ValidationInfo = form.validate.getOrElse(null)

  def form = Early.form

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

  private def caretSharingClients = {
    projectInfo.toSeq
      .flatMap(_.clients)
      .filter(_.workingMode == Some(CaretSharingModeRequest))
      .map(_.name)
  }

  private def followClients = {
    projectInfo.toSeq
      .flatMap(_.clients)
      .filter(_.workingMode.exists(_.isInstanceOf[FollowModeRequest]))
      .map(c => c.workingMode.get.asInstanceOf[FollowModeRequest].name -> c.name)
      .groupBy(_._1)
      .map(kv => kv._1 -> kv._2.map(_._2))
  }

}
