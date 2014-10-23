package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.client.ClientContextHolder

trait PublishEvents extends ClientContextHolder {
  def publishEvent(event: PairEvent) = {
    context.foreach { ctx =>
      val line = event.toMessage
      ctx.writeAndFlush(line)
      println(s"<${ctx.name}> send to server: $line")
    }
  }

}
