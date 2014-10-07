package com.thoughtworks.pli.intellij.remotepair

import io.netty.channel.ChannelHandlerContext

package object server {

  implicit class CustomContext(context: ChannelHandlerContext) {
    def writeEvent(event: PairEvent) = {
      context.writeAndFlush(event.toMessage)
    }

  }

}
