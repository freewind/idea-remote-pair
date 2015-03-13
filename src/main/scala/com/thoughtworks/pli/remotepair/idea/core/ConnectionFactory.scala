package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import io.netty.channel.ChannelHandlerContext
import com.intellij.openapi.diagnostic.Logger

object ConnectionFactory {
  type Connection = ConnectionFactory#create
}

case class ConnectionFactory(logger: Logger) {

  case class create(raw: ChannelHandlerContext) {
    def publish(event: PairEvent): Unit = {
      logger.info(s"<client> publish to server: ${event.toMessage}")
      raw.writeAndFlush(event.toMessage)
    }
    def close() = raw.close()
  }

}

