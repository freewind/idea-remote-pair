package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.util.messages.MessageBusConnection
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl
import com.thoughtworks.pli.remotepair.idea.project.GetMessageBus

class CreateMessageConnection(getMessageBus: GetMessageBus, currentProject: IdeaProjectImpl) {
  def apply(): Option[MessageBusConnection] = {
    getMessageBus().map(_.connect(currentProject.rawProject))
  }
}
