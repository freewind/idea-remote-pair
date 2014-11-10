package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.PairEvent

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  var name: String = "Unknown"

  var ip: String = "Unknown"

  val pathSpecifiedLocks = new PathSpecifiedLocks

  val projectSpecifiedLocks = new ProjectSpecifiedLocks

  var myWorkingMode: Option[MyWorkingMode] = None

  def writeEvent(event: PairEvent) = {
    println("########## write message to " + name + ": " + event.toMessage)
    context.writeAndFlush(event.toMessage)
  }

  def hasUserInformation: Boolean = {
    ip != "Unknown" && name != "Unknown"
  }

  def isFollowing(name: ContextData) = myWorkingMode match {
    case Some(FollowMode(star)) if star == name.name => true
    case _ => false
  }

  def isSharingCaret = myWorkingMode match {
    case Some(CaretSharingMode) => true
    case _ => false
  }
}

sealed trait MyWorkingMode

case object CaretSharingMode extends MyWorkingMode

case class FollowMode(star: String) extends MyWorkingMode

case object ParallelMode extends MyWorkingMode

class ProjectSpecifiedLocks {
  val activeTabLocks = new EventLocks[String]
}