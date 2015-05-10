package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus

class GetMessageBus(currentProject: Project) {
  def apply(): Option[MessageBus] = {
    if (currentProject.isDisposed) None else Some(currentProject.getMessageBus)
  }
}
