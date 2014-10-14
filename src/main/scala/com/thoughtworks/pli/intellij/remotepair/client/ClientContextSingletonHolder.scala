package com.thoughtworks.pli.intellij.remotepair.client

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{ServerStatusHolder, ClientContextHolder, ServerStatusResponse}

trait ClientContextSingletonHolder extends ClientContextHolder {
  override def context: Option[ChannelHandlerContext] = ClientObjects.context
  override def context_=(context: Option[ChannelHandlerContext]): Unit = ClientObjects.context = context
}
