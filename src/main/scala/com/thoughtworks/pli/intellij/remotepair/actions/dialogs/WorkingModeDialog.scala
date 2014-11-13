package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.{ClientInfoHolder, ServerStatusHolder}
import com.thoughtworks.pli.intellij.remotepair.{FollowModeRequest, CaretSharingModeRequest, PublishEvents, InvokeLater}
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class WorkingModeDialog(project: Project) extends DialogWrapper(project) with ServerStatusHolder with InvokeLater with PublishEvents with ClientInfoHolder {

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

  private def caretSharingClients = {
    myProject.toSeq
      .flatMap(_.clients)
      .filter(_.workingMode == Some(CaretSharingModeRequest))
      .map(_.name)
  }

  private def followClients = {
    myProject.toSeq
      .flatMap(_.clients)
      .filter(_.workingMode.exists(_.isInstanceOf[FollowModeRequest]))
      .map(c => c.workingMode.get.asInstanceOf[FollowModeRequest].name -> c.name)
      .groupBy(_._1)
      .map(kv => kv._1 -> kv._2.map(_._2))
  }

  private def myProject = {
    println("#########: " + serverStatus)
    println("#########: " + clientInfo)
    val xxx = for {
      server <- serverStatus
      client <- clientInfo
      projectName <- client.project
      p <- server.projects.find(_.name == projectName)
    } yield p
    println("########: " + xxx)
    xxx
  }

}
