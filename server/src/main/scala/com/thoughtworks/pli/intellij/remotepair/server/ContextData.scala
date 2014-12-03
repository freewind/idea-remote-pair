package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair._

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  var name: String = "Unknown"

  var ip: String = "Unknown"

  val pathSpecifiedLocks = new PathSpecifiedLocks

  val projectSpecifiedLocks = new ProjectSpecifiedLocks

  def writeEvent(event: PairEvent) = {
    println("########## server's gonna send message to " + name + ": " + event.toMessage)
    context.writeAndFlush(event.toMessage)
  }

  def hasUserInformation: Boolean = {
    ip != "Unknown" && name != "Unknown"
  }

}

class ProjectSpecifiedLocks {
  val activeTabLocks = new EventLocks[String]
}
