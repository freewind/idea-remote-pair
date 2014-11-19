package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{Messages, ValidationInfo, DialogWrapper}
import com.thoughtworks.pli.intellij.remotepair.actions.forms.JoinProjectForm
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.client.ServerStatusHolder
import com.thoughtworks.pli.intellij.remotepair.{InvokeLater, JoinProjectRequest, CreateProjectRequest, PublishEvents}

class JoinProjectDialog(project: Project) extends DialogWrapper(project) with ServerStatusHolder with PublishEvents with InvokeLater {

  init()

  private object EarlyInit {
    val form = new JoinProjectForm
  }

  def form = EarlyInit.form

  override def createCenterPanel(): JComponent = {
    form.setExistingProjects(getExistingProjects)
    form.getMainPanel
  }

  override def doValidate(): ValidationInfo = form.validate().getOrElse(null)

  override def doOKAction(): Unit = {
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        (form.getNewProjectName, form.getExistingProjectName) match {
          case (Some(p), _) => publishEvent(new CreateProjectRequest(p))
          case (_, Some(p)) => publishEvent(new JoinProjectRequest(p))
          case _ =>
        }
      } catch {
        case e: Throwable => showError(e.toString)
      }
    }
  }

  def getExistingProjects: Seq[String] = serverStatus.toSeq.flatMap(_.projects.map(_.name))

  def showError(message: String) {
    Messages.showErrorDialog(project, message, "Error")
  }
}
