package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.RichProject
import io.netty.channel.ChannelHandlerContext

trait ClientContextHolder {
  this: CurrentProjectHolder =>
  def context: Option[ChannelHandlerContext] = ClientContextHolder.context.get(currentProject)
  def context_=(context: Option[ChannelHandlerContext]): Unit = context match {
    case Some(c) => ClientContextHolder.context += (currentProject -> c)
    case None => ClientContextHolder.context -= currentProject
  }
}

object ClientContextHolder {
  var context: Map[RichProject, ChannelHandlerContext] = Map.empty[RichProject, ChannelHandlerContext]
}

