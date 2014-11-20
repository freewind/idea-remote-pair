package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.client.{ClientInfoHolder, ClientContextHolder}

trait PublishEvents extends ClientContextHolder with ClientInfoHolder {
  def publishEvent(event: PairEvent) = {
    context.foreach { ctx =>
      val line = event.toMessage
      ctx.writeAndFlush(line)
      println(s"<${clientInfo.map(_.name).getOrElse("Unknown")}> publish to server: $line")
    }
  }
}
