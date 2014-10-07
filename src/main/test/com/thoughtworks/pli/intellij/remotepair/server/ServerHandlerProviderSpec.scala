package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import io.netty.channel.ChannelHandlerContext
import org.specs2.mock.Mockito
import io.netty.buffer.{ByteBufAllocator, ByteBuf}
import com.thoughtworks.pli.intellij.remotepair._
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.OpenTabEvent
import com.thoughtworks.pli.intellij.remotepair.ContentChangeEvent
import com.thoughtworks.pli.intellij.remotepair.ResetContentEvent

class ServerHandlerProviderSpec extends Specification with Mockito {

  "ServerHandlerProvider" should {
    "create new server handler" in new Mocking {
      handler !== null
    }
  }

  "ServerHandler" should {
    "add the context to global cache when channelActive" in new Mocking {
      handler.channelActive(context1)
      provider.contexts.size === 1
    }
    "remove the context from global cache when channel is inactive" in new Mocking {
      provider.contexts.add(context1)
      handler.channelInactive(context1)
      provider.contexts.size === 0
    }
    "can only handle ByteBuf message type" in new Mocking {
      provider.contexts.add(context1)
      provider.contexts.add(context2)

      handler.channelRead(context1, "unknown-message-type")

      there was no(context2).writeAndFlush(any)
    }
    "broadcast received event to other context" in new Mocking {
      broadcastEvent(context1, contentChangeEventA1, context2)

      there was one(context2).writeAndFlush(any)
      there was no(context1).writeAndFlush(any)
    }
  }

  "Content event locks" should {
    "be added from sent ContentChangeEvent" in new Mocking {
      broadcastEvent(context1, contentChangeEventA1, context2)

      provider.contexts.get(context2) must beSome.which(_.contentLocks.size === 1)
    }
    "be added from ContentChangeEvent from different sources" in new Mocking {
      broadcastEvent(context1, contentChangeEventA1, context2)
      broadcastEvent(context3, contentChangeEventA1, context2)

      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.size === 1
        data.contentLocks.get("/aaa").map(_.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new Mocking {
      broadcastEvent(context1, contentChangeEventA1, context2)
      broadcastEvent(context2, contentChangeEventA1SameSummary, context1)
      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.get("/aaa").map(_.size) === Some(0)
      }
      there was no(context1).writeAndFlush(any)
    }
    "send a ContentResetRequestEvent if the feedback event is not matched for the same file path" in new Mocking {
      setMaster(context1)

      broadcastEvent(context1, contentChangeEventA1, context2)
      broadcastEvent(context2, contentChangeEventA2, context1)

      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.get("/aaa").map(_.size) === Some(1)
      }

      there was one(context1).writeAndFlush("ContentResetRequestEvent {\"path\":\"/aaa\"}\n")
      there was no(context2).writeAndFlush("ContentResetRequestEvent {\"path\":\"/aaa\"}\n")
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      broadcastEvent(context1, contentChangeEventA1, context2)
      broadcastEvent(context1, contentChangeEventA2, context2)
      broadcastEvent(context1, resetContentEvent, context2)

      dataOf(context2).flatMap(_.contentLocks.get("/aaa")) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      setMaster(context1)
      broadcastEvent(context2, contentChangeEventA1, context1)
      broadcastEvent(context1, resetContentEvent, context2)

      dataOf(context1).flatMap(_.contentLocks.get("/aaa")) must beSome.which(_.size === 0)
    }
  }


  "Master context" should {
    "be the first one if no one requested change" in new Mocking {
      handler.channelActive(context1)
      handler.channelActive(context2)

      dataOf(context1).map(_.master) === Some(true)
      dataOf(context2).map(_.master) === Some(false)
    }
    "will change to next one automatically if the master is disconnected" in new Mocking {
      handler.channelActive(context1)
      handler.channelActive(context2)

      handler.channelInactive(context1)
      dataOf(context2).map(_.master) === Some(true)
    }
    "changed to the one which is requested" in new Mocking {
      handler.channelActive(context1)
      handler.channelActive(context2)

      handler.channelRead(context2, "BeMasterEvent {}")

      dataOf(context1).map(_.master) === Some(false)
      dataOf(context2).map(_.master) === Some(true)
    }
  }

  "OpenTabEvent" should {
    "be a lock when it sent" in new Mocking {
      broadcastEvent(context1, openTabEvent1, context2)

      dataOf(context2).map(_.activeTabLocks.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      broadcastEvent(context1, openTabEvent1, context2)
      broadcastEvent(context2, openTabEvent1, context1)

      dataOf(context2).map(_.activeTabLocks.size) === Some(0)
      dataOf(context1).map(_.activeTabLocks.size) === Some(0)
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new Mocking {
      setMaster(context1)
      broadcastEvent(context1, openTabEvent1, context2)
      broadcastEvent(context2, openTabEvent2, context1)

      there was one(context1).writeAndFlush("TabResetRequestEvent {}\n")
      there was no(context2).writeAndFlush("TabResetRequestEvent {}\n")
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      broadcastEvent(context1, openTabEvent1, context2)
      broadcastEvent(context1, tabResetEvent, context2)

      dataOf(context2).map(_.activeTabLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some("/ccc")
      }
    }
    "clear the master locks as well" in new Mocking {
      setMaster(context1)
      broadcastEvent(context2, openTabEvent1, context1)
      broadcastEvent(context1, tabResetEvent, context2)

      dataOf(context1).map(_.activeTabLocks) must beSome.which { locks =>
        locks.size === 0
      }
    }
  }

  trait Mocking extends Scope with MockEvents {
    def mockContext: ChannelHandlerContext = {
      val c = mock[ChannelHandlerContext]
      c.alloc() returns mock[ByteBufAllocator]
      c.alloc().buffer() returns mock[ByteBuf]
      c
    }

    val provider = new ServerHandlerProvider with ContextHolderProvider {
      override val contexts = new ContextHolder
    }

    def dataOf(context: ChannelHandlerContext) = {
      provider.contexts.get(context)
    }

    def setMaster(context: ChannelHandlerContext) {
      if (!provider.contexts.contains(context)) {
        provider.contexts.add(context1)
      }
      dataOf(context1).foreach(_.master = true)
    }

    val handler = provider.createServerHandler()
    val context1 = mockContext
    val context2 = mockContext
    val context3 = mockContext

    def broadcastEvent(from: ChannelHandlerContext, event: PairEvent, to: ChannelHandlerContext) = {
      Seq(from, to).filterNot(provider.contexts.contains).foreach(provider.contexts.add)
      println("########################################")
      println(event.toMessage)
      handler.channelRead(from, event.toMessage)
    }

  }

  trait MockEvents {
    val contentChangeEventA1 = ContentChangeEvent("/aaa", 10, "aa1", "bb1", "s1")
    val contentChangeEventA1SameSummary = ContentChangeEvent("/aaa", 100, "aaaaaa1", "bbbbbbbbb1", "s1")
    val contentChangeEventA2 = ContentChangeEvent("/aaa", 20, "aa2", "bb2", "s2")
    val contentChangeEventB1 = ContentChangeEvent("/bbb", 30, "aa3", "bb3", "s3")
    val resetContentEvent = ResetContentEvent("/aaa", "new-content", "s4")
    val openTabEvent1 = OpenTabEvent("/aaa")
    val openTabEvent2 = OpenTabEvent("/bbb")
    val tabResetEvent = ResetTabEvent("/ccc")
  }

}
