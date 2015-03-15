package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.util.messages.{MessageBus, Topic}


class NotifyChanges(getMessageBus: GetMessageBus) {
  def apply(): Unit = {
    getMessageBus().foreach(ProjectStatusChanges.notify)
  }
}


object ProjectStatusChanges {
  
  val ProjectStatusTopic: Topic[Listener] =
    Topic.create("Project status notifications", classOf[Listener])

  def notify(messageBus: MessageBus) = {
    messageBus.syncPublisher(ProjectStatusChanges.ProjectStatusTopic).onChange()
  }

  trait Listener {
    def onChange(): Unit
  }

}
