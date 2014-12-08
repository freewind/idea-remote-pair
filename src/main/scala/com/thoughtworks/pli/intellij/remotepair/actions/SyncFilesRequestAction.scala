package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.SendSyncFilesRequestDialog

class SyncFilesRequestAction extends AnAction("Sync all files") {
  override def actionPerformed(event: AnActionEvent): Unit = {
    createDialog(Projects.init(event.getProject)).show()
  }

  def createDialog(project: RichProject) = new SendSyncFilesRequestDialog(project)
}
