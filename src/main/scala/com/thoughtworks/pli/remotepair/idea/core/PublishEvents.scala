package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent

trait PublishEvents extends AppLogger {
  this: CurrentProjectHolder =>

  def publishEvent(event: PairEvent) = {
    currentProject.connection.foreach { conn =>
      conn.publish(event)
    }
  }

}
