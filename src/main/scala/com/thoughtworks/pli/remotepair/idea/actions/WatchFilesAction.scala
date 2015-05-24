package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.idea.Module

class WatchFilesAction extends AnAction("Show watching files") {

  override def actionPerformed(event: AnActionEvent): Unit = {
    val dialog = createDialog(event.getProject)
    dialog.showOnCenter()
  }

  def createDialog(project: Project) = new Module {
    override def currentIdeaRawProject = project
  }.dialogFactories.createWatchFilesDialog(None)

}
