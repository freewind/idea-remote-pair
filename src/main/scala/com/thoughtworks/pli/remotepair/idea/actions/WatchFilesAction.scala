package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.idea.Module
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog

class WatchFilesAction extends AnAction("Show watching files") {

  override def actionPerformed(event: AnActionEvent): Unit = {
    val dialog = createDialog(event.getProject)
    dialog.showOnCenter()
  }

  def createDialog(project: Project): WatchFilesDialog = new Module {
    override def currentIdeaProject = project
  }.watchFilesDialogFactory(None)

}
