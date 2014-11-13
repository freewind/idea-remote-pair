package com.thoughtworks.pli.intellij.remotepair.client

import io.netty.channel.ChannelHandlerContext

trait ClientContextHolder {
  def context: Option[ChannelHandlerContext] = ClientContextHolder.context
  def context_=(context: Option[ChannelHandlerContext]): Unit = ClientContextHolder.context = context
}

object ClientContextHolder {
  var context: Option[ChannelHandlerContext] = None
}
