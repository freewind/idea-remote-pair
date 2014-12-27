package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent

trait PublishEvents extends AppLogger {
  this: CurrentProjectHolder =>

  def publishEvent(event: PairEvent) = {
    currentProject.context.foreach { ctx =>
      val line = event.toMessage
      log.info(s"<client> ${currentProject.clientInfo.map(_.name)} -> Server: $line")

      ctx.writeAndFlush(line)
    }
  }

}
