package com.thoughtworks.pli.intellij.remotepair

import io.netty.channel.ChannelHandlerContext

package object server {

  implicit class CustomContext(context: ChannelHandlerContext) {
    def writeEvent(event: PairEvent) = {
      writeLineAndFlush(event.toMessage)
    }

    def writeLineAndFlush(line: String) = {
      context.writeAndFlush(line + "\n")
    }
  }

}
