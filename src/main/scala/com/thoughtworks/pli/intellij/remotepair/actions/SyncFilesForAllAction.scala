package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support

class SyncFilesForAllAction extends AnAction("Sync all files") {
  override def actionPerformed(event: AnActionEvent): Unit = {
    new Request(Projects.init(event.getProject)).publish()
  }

  class Request(override val currentProject: RichProject) extends PublishEvents with CurrentProjectHolder with Md5Support {
    def publish(): Unit = {
      publishEvent(SyncFilesForAll)
    }
  }

}
