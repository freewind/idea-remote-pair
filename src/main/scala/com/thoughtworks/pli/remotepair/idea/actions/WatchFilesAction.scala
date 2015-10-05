package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.remotepair.idea.RemotePairProjectComponent

class WatchFilesAction extends AnAction("Show watching files") {

  override def actionPerformed(event: AnActionEvent): Unit = {
    val module = event.getProject.getUserData(RemotePairProjectComponent.moduleKey)
    val dialog = module.dialogFactories.createWatchFilesDialog(None)
    dialog.showOnCenter()
  }

}
