package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.idea.Module
import com.thoughtworks.pli.remotepair.idea.dialogs.ChooseIgnoreDialogFactory.ChooseIgnoreDialog

class IgnoreFilesAction extends AnAction("Show ignore files") {

  override def actionPerformed(event: AnActionEvent): Unit = {
    val dialog = createDialog(event.getProject)
    dialog.showOnCenter()
  }

  def createDialog(project: Project): ChooseIgnoreDialog = new Module {
    override def rawProject: Project = project
  }.chooseIgnoreDialogFactory.create()

}
