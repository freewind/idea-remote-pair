package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}

object Connection {
  type Factory = (ChannelHandlerContext) => Connection
}

class Connection(raw: ChannelHandlerContext)(logger: PluginLogger) {
  def publish(event: PairEvent): ChannelFuture = {
    logger.info(s"publish to server: ${event.toMessage}")
    raw.writeAndFlush(event.toMessage)
  }
  def close() = raw.close()
}
