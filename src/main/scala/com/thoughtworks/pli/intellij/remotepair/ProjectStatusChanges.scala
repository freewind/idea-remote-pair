package com.thoughtworks.pli.intellij.remotepair

import com.intellij.util.messages.{MessageBus, Topic}

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
