package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.forms.JoinProjectForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class JoinProjectDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with PublishEvents with InvokeLater with CurrentProjectHolder {

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

  def getExistingProjects: Seq[String] = currentProject.serverStatus.toSeq.flatMap(_.projects.map(_.name))

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

}
