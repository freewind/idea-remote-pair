package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class PublishEvent(currentProject: RichProject) {

  def apply(event: PairEvent) = {
    currentProject.connection.foreach { conn =>
      conn.publish(event)
    }
  }

}
