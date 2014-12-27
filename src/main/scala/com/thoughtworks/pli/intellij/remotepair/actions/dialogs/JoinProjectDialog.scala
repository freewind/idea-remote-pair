package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.forms.{ProjectWithMemberNames, JoinProjectForm}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.protocol.{JoinProjectRequest, CreateProjectRequest}
import com.thoughtworks.pli.intellij.remotepair.settings.AppSettingsProperties

class JoinProjectDialog(override val currentProject: RichProject, message: Option[String]) extends DialogWrapper(currentProject.raw) with PublishEvents with InvokeLater with CurrentProjectHolder with AppSettingsProperties {

  init()

  private object EarlyInit {
    val form = new JoinProjectForm
  }

  def form = EarlyInit.form

  override def createCenterPanel(): JComponent = {
    val projects = getExistingProjects
    if (projects.isEmpty) {
      form.getRadioNewProject.setSelected(true)
    } else {
      form.setExistingProjects(projects)
    }
    message match {
      case Some(msg) => form.showPreErrorMessage(msg)
      case None => form.hidePreErrorMessage()
    }
    restoreInputValues()
    form.getMainPanel
  }

  override def doValidate(): ValidationInfo = form.validate().orNull

  override def doOKAction(): Unit = {
    storeInputValues()
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        (form.getNewProjectName, form.getExistingProjectName) match {
          case (Some(p), _) => publishEvent(new CreateProjectRequest(p, form.clientName))
          case (_, Some(p)) => publishEvent(new JoinProjectRequest(p, form.clientName))
          case _ =>
        }
      } catch {
        case e: Throwable => showError(e.toString)
      }
    }
  }

  def getExistingProjects: Seq[ProjectWithMemberNames] = currentProject.serverStatus.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }
  override def getPreferredFocusedComponent: JComponent = {
    if (form.existingProjectRadios.isEmpty)
      form.getTxtNewProjectName
    else
      form.existingProjectRadios.head
  }

  private def storeInputValues(): Unit = {
    appProperties.clientName = form.clientName
  }

  private def restoreInputValues(): Unit = {
    form.clientName = appProperties.clientName
  }
}
