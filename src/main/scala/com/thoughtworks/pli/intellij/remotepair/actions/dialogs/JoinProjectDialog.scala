package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.forms.JoinProjectForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class JoinProjectDialog(override val currentProject: RichProject, message: Option[String]) extends DialogWrapper(currentProject.raw) with PublishEvents with InvokeLater with CurrentProjectHolder {

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
    form.getMainPanel
  }

  override def doValidate(): ValidationInfo = form.validate().orNull

  override def doOKAction(): Unit = {
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        (form.getNewProjectName, form.getExistingProjectName) match {
          case (Some(p), _) => publishEvent(new CreateProjectRequest(p, form.getClientName))
          case (_, Some(p)) => publishEvent(new JoinProjectRequest(p, form.getClientName))
          case _ =>
        }
      } catch {
        case e: Throwable => showError(e.toString)
      }
    }
  }

  def getExistingProjects: Seq[String] = currentProject.serverStatus.toSeq.flatMap(_.projects.map(_.name))

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }
  override def getPreferredFocusedComponent: JComponent = {
    if (form.existingProjectRadios.isEmpty)
      form.getTxtNewProjectName
    else
      form.existingProjectRadios.head
  }
}
