package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

trait PublishEvents extends AppLogger {
  this: CurrentProjectHolder =>

  def publishEvent(event: PairEvent) = {
    currentProject.context.foreach { ctx =>
      val line = event.toMessage
      log.info(s"<${currentProject.clientInfo.map(_.name).getOrElse("Unknown")}> publish to server: $line")

      ctx.writeAndFlush(line)
    }
  }

}
