package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{DialogWrapper, Messages}
import com.thoughtworks.pli.intellij.remotepair.actions.forms.IgnoreFilesForm
import com.thoughtworks.pli.intellij.remotepair.client.{ClientInfoHolder, ServerStatusHolder}
import com.thoughtworks.pli.intellij.remotepair.{IgnoreFilesRequest, InvokeLater, PublishEvents}

class IgnoreFilesDialog(project: Project) extends DialogWrapper(project) with ServerStatusHolder with PublishEvents with InvokeLater with ClientInfoHolder {

  init()

  private object EarlyInit {
    val form = new IgnoreFilesForm
  }

  def form = EarlyInit.form

  override def createCenterPanel(): JComponent = {
    form.getFilesContext.setText(getServerIgnoreFiles)
    form.getMainPanel
  }

  override def doOKAction(): Unit = {
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
    Messages.showErrorDialog(project, message, "Error")
  }

  private def getServerIgnoreFiles = projectInfo.fold("")(_.ignoredFiles.mkString("\n"))

}
