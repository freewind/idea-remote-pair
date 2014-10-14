package com.thoughtworks.pli.intellij.remotepair.client

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{ServerStatusHolder, ClientContextHolder, ServerStatusResponse}

object ClientObjects {
  var context: Option[ChannelHandlerContext] = None
  var serverStatus: Option[ServerStatusResponse] = None
}
