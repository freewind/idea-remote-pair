package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import javax.swing.JComponent

import com.intellij.openapi.ui.DialogWrapper
import com.thoughtworks.pli.intellij.remotepair.RichProject
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ChooseIgnoreForm

class ChooseIgnoreDialog(currentProject: RichProject) extends DialogWrapper(currentProject.raw) {
  init()

  private object EarlyInit {
    val form = new ChooseIgnoreForm(currentProject)
  }

  private def form = EarlyInit.form

  override def createCenterPanel() = {
    form.setWorkingDir(currentProject.getBaseDir)
    form.ignoredFiles = currentProject.projectInfo.map(_.ignoredFiles).getOrElse(Nil)
    form.getMainPanel
  }


}
