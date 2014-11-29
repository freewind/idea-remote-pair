package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ChooseIgnoreDialog
import com.thoughtworks.pli.intellij.remotepair.{InvokeLater, Projects}

class IgnoreFilesAction extends AnAction("Show ignore files") with InvokeLater {

  override def actionPerformed(event: AnActionEvent): Unit = {
    val dialog = createDialog(event.getProject)
    dialog.show()
  }

  def createDialog(project: Project) = new ChooseIgnoreDialog(Projects.init(project))

}
