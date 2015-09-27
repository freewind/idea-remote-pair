package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleJoinedToProjectEvent(project: MyProject, myIde: MyIde) {

  def apply(event: JoinedToProjectEvent): Unit = myIde.runWriteAction {
    project.openedFiles.foreach(project.close)
  }

}
