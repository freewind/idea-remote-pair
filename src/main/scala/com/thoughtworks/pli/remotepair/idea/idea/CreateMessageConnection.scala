package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.thoughtworks.pli.remotepair.idea.project.GetMessageBus

class CreateMessageConnection(getMessageBus: GetMessageBus, currentProject: Project) {
  def apply(): Option[MessageBusConnection] = {
    getMessageBus().map(_.connect(currentProject))
  }
}
