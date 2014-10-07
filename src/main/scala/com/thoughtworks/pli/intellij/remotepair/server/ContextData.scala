package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{MoveCaretEvent, OpenTabEvent, ContentChangeEvent}
import scala.collection.concurrent.TrieMap

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  val contentLocks = new ContentSummaryLocks

  val activeTabLocks = new EventLocks[String]

  val caretPositionLocks = new EventLocks[MoveCaretEvent]


}
