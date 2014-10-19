package com.thoughtworks.pli.intellij.remotepair.client

import io.netty.channel.ChannelHandlerContext

trait ClientContextHolder {
  def context: Option[ChannelHandlerContext] = ClientObjects.context
  def context_=(context: Option[ChannelHandlerContext]): Unit = ClientObjects.context = context
}
