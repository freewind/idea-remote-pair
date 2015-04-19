package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}
import com.intellij.openapi.diagnostic.Logger

object Connection {
  type Factory = (ChannelHandlerContext) => Connection
}

class Connection(raw: ChannelHandlerContext)(logger: Logger) {
  def publish(event: PairEvent): ChannelFuture = {
    logger.info(s"<client> publish to server: ${event.toMessage}")
    raw.writeAndFlush(event.toMessage)
  }
  def close() = raw.close()
}
