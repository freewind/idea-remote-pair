package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}

class HandleJoinedToProjectEvent(project: MyProject, myPlatform: MyPlatform) {

  def apply(event: JoinedToProjectEvent): Unit = myPlatform.runWriteAction {
    project.openedFiles.foreach(_.close())
  }

}
