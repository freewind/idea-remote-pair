package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection

class CreateMessageConnection(getMessageBus: GetMessageBus, currentProject: Project) {
  def apply(): Option[MessageBusConnection] = {
    getMessageBus().map(_.connect(currentProject))
  }
}
