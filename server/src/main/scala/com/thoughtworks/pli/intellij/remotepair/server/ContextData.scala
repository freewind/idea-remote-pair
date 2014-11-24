package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair._

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  var name: String = "Unknown"

  var ip: String = "Unknown"

  val pathSpecifiedLocks = new PathSpecifiedLocks

  val projectSpecifiedLocks = new ProjectSpecifiedLocks

  var myWorkingMode: Option[WorkingModeEvent] = Some(CaretSharingModeRequest)

  def writeEvent(event: PairEvent) = {
    println("########## write message to " + name + ": " + event.toMessage)
    context.writeAndFlush(event.toMessage)
  }

  def hasUserInformation: Boolean = {
    ip != "Unknown" && name != "Unknown"
  }

  def isSharingCaret = myWorkingMode match {
    case Some(CaretSharingModeRequest) => true
    case _ => false
  }
}

class ProjectSpecifiedLocks {
  val activeTabLocks = new EventLocks[String]
}
