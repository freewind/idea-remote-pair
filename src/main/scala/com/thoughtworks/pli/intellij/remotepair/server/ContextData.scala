package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{PairEvent, MoveCaretEvent, OpenTabEvent, ChangeContentEvent}
import scala.collection.concurrent.TrieMap

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  var name: String = "Unknown"

  var ip: String = "Unknown"

  val contentLocks = new ContentSummaryLocks

  val activeTabLocks = new EventLocks[String]

  val caretPositionLocks = new EventLocks[MoveCaretEvent]

  def writeEvent(event: PairEvent) = {
    context.writeAndFlush(event.toMessage)
  }

}
