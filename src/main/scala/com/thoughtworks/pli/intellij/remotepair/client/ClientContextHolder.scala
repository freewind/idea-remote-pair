package com.thoughtworks.pli.intellij.remotepair.client

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.ClientInfoResponse

trait ClientContextHolder {
  def context: Option[ChannelHandlerContext] = ClientContextHolder.context
  def context_=(context: Option[ChannelHandlerContext]): Unit = ClientContextHolder.context = context
}

object ClientContextHolder {
  var context: Option[ChannelHandlerContext] = None
}


trait ClientInfoHolder extends ServerStatusHolder {
  def clientInfo: Option[ClientInfoResponse] = ClientInfoHolder.clientInfo
  def clientInfo_=(info: Option[ClientInfoResponse]) = ClientInfoHolder.clientInfo = info

  def projectInfo = for {
    server <- serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

}

object ClientInfoHolder {
  var clientInfo: Option[ClientInfoResponse] = None
}