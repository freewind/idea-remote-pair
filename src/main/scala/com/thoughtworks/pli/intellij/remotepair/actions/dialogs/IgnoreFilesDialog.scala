package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.DialogWrapper
import com.thoughtworks.pli.intellij.remotepair.actions.forms.IgnoreFilesForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.{IgnoreFilesRequest, InvokeLater, PublishEvents, RichProject}

class IgnoreFilesDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with PublishEvents with InvokeLater with CurrentProjectHolder {
  self =>

  init()

  private object EarlyInit {
    val form = new IgnoreFilesForm with CurrentProjectHolder {
      val currentProject = self.currentProject
    }
  }

  def form = EarlyInit.form

  override def createCenterPanel(): JComponent = {
    form.getFilesContext.setText(getServerIgnoreFiles)
    form.getMainPanel
  }

  override def doOKAction(): Unit = {
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        publishEvent(IgnoreFilesRequest(form.getFileList))
        println("****************** published!!!!!!!")
      } catch {
        case e: Throwable =>
          println("###################### some error: " + e)
          showError(e.toString)
      }
    }
  }

  def showError(message: String) {
    currentProject.showErrorDialog("Error", message)
  }

  private def getServerIgnoreFiles = currentProject.projectInfo.fold("")(_.ignoredFiles.mkString("\n"))

  override def getPreferredFocusedComponent: JComponent = form.getFilesContext
}
