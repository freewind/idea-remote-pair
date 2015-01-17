package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.remotepair.idea.core.{RichProject, Projects}
import com.thoughtworks.pli.remotepair.idea.dialogs.SyncFilesOptionDialog

class SyncFilesRequestAction extends AnAction("Sync all files") {
  override def actionPerformed(event: AnActionEvent): Unit = {
    createDialog(Projects.init(event.getProject)).showOnCenter()
  }

  def createDialog(project: RichProject) = new SyncFilesOptionDialog(project)
}
