package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ChooseIgnoreForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.{IgnoreFilesRequest, InvokeLater, PublishEvents, RichProject}

class ChooseIgnoreDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with InvokeLater with PublishEvents with CurrentProjectHolder {
  init()

  private object EarlyInit {
    val form = new ChooseIgnoreForm(currentProject)
  }

  private def form = EarlyInit.form

  override def createCenterPanel() = {
    form.setWorkingDir(currentProject.getBaseDir)
    form.ignoredFiles = currentProject.projectInfo.map(_.ignoredFiles).getOrElse(getDotFiles)
    form.getMainPanel
  }

  override def doOKAction(): Unit = {
    close(DialogWrapper.OK_EXIT_CODE)
    invokeLater {
      try {
        publishEvent(IgnoreFilesRequest(form.ignoredFiles))
      } catch {
        case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
      }
    }
  }

  private def getDotFiles: Seq[String] = {
    currentProject.getBaseDir.getChildren.filter(_.getName.startsWith(".")).map(currentProject.getRelativePath)
  }

}