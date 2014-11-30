package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.{Projects, PublishEvents, RichProject, SyncFilesRequest}

class SyncFilesRequestAction extends AnAction("Sync all files") {
  override def actionPerformed(event: AnActionEvent): Unit = {
    new Request(Projects.init(event.getProject)).syncAll()
  }

  class Request(override val currentProject: RichProject) extends PublishEvents with CurrentProjectHolder {
    def syncAll(): Unit = {
      publishEvent(SyncFilesRequest)
    }
  }

}
