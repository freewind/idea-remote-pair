package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.util.messages.MessageBus
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetMessageBus(currentProject: IdeaProjectImpl) {
  def apply(): Option[MessageBus] = {
    if (currentProject.raw.isDisposed) None else Some(currentProject.raw.getMessageBus)
  }
}
