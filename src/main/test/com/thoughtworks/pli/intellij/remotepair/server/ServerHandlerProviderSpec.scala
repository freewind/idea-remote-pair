package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import io.netty.channel.ChannelHandlerContext
import org.specs2.mock.Mockito
import io.netty.buffer.{ByteBufAllocator, ByteBuf}
import com.thoughtworks.pli.intellij.remotepair._
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.OpenTabEvent
import com.thoughtworks.pli.intellij.remotepair.ChangeContentEvent
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
      activeContexts(context1, context2)
      handler.channelRead(context1, changeContentEventA1.toMessage)

      there was one(context2).writeAndFlush(any)
      there was no(context1).writeAndFlush(any)
    }
  }

  "Content event locks" should {
    "be added from sent ChangeContentEvent" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, changeContentEventA1.toMessage)

      provider.contexts.get(context2) must beSome.which(_.contentLocks.size === 1)
    }
    "be added from ChangeContentEvent from different sources" in new Mocking {
      activeContexts(context1, context2, context3)
      handler.channelRead(context1, changeContentEventA1.toMessage)
      handler.channelRead(context3, changeContentEventA1.toMessage) // unlock
      handler.channelRead(context3, changeContentEventA2.toMessage)

      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.size === 1
        data.contentLocks.get("/aaa").map(_.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, changeContentEventA1.toMessage)
      handler.channelRead(context2, changeContentEventA1SameSummary.toMessage)

      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.get("/aaa").map(_.size) === Some(0)
      }
      there was no(context1).writeAndFlush(any)
    }
    "send a ResetContentRequest if the feedback event is not matched for the same file path" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, changeContentEventA1.toMessage)
      handler.channelRead(context2, changeContentEventA2.toMessage)

      provider.contexts.get(context2) must beSome.which { data =>
        data.contentLocks.get("/aaa").map(_.size) === Some(1)
      }

      there was one(context1).writeAndFlush(ResetContentRequest("/aaa").toMessage)
      there was no(context2).writeAndFlush(ResetContentRequest("/aaa").toMessage)
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, changeContentEventA1.toMessage)
      handler.channelRead(context1, changeContentEventA2.toMessage)
      handler.channelRead(context1, resetContentEvent.toMessage)

      dataOf(context2).flatMap(_.contentLocks.get("/aaa")) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context2, changeContentEventA1.toMessage)
      handler.channelRead(context1, resetContentEvent.toMessage)

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
      handler.channelRead(context2, clientInfoEvent2.toMessage)

      handler.channelRead(context1, changeMasterEvent.toMessage)

      dataOf(context1).map(_.master) === Some(false)
      dataOf(context2).map(_.master) === Some(true)
    }
    "response error message if specified name is not exist" in new Mocking {
      handler.channelActive(context1)
      handler.channelRead(context1, clientInfoEvent.toMessage)
      handler.channelRead(context1, changeMasterEvent.toMessage)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }

  "OpenTabEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, openTabEvent1.toMessage)

      dataOf(context2).map(_.activeTabLocks.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, openTabEvent1.toMessage)
      handler.channelRead(context2, openTabEvent1.toMessage)

      dataOf(context2).map(_.activeTabLocks.size) === Some(0)
      dataOf(context1).map(_.activeTabLocks.size) === Some(0)
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, openTabEvent1.toMessage)
      handler.channelRead(context2, openTabEvent2.toMessage)

      there was one(context1).writeAndFlush(ResetTabRequest().toMessage)
      there was no(context2).writeAndFlush(ResetTabRequest().toMessage)
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, openTabEvent1.toMessage)
      handler.channelRead(context1, tabResetEvent.toMessage)

      dataOf(context2).map(_.activeTabLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some("/ccc")
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context2, openTabEvent1.toMessage)
      handler.channelRead(context1, tabResetEvent.toMessage)

      dataOf(context1).map(_.activeTabLocks) must beSome.which { locks =>
        locks.size === 0
      }
    }
  }

  "ClientInfoEvent" should {
    "store client name and ip to context data" in new Mocking {
      activeContexts(context1)
      handler.channelRead(context1, clientInfoEvent.toMessage)

      dataOf(context1).map(_.name) === Some("Freewind")
      dataOf(context1).map(_.ip) === Some("1.1.1.1")
    }
    "get an error back if the name is blank" in new Mocking {
      activeContexts(context1)
      handler.channelRead(context1, ClientInfoEvent("non-empty-ip", "  ").toMessage)
      there was one(context1).writeAndFlush(ServerErrorResponse("Name is not provided").toMessage)
    }
    "get an error back if the name is already existing" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, ClientInfoEvent("non-empty-ip", "Freewind").toMessage)
      handler.channelRead(context2, ClientInfoEvent("non-empty-ip", "Freewind").toMessage)
      there was one(context2).writeAndFlush(ServerErrorResponse("Specified name 'Freewind' is already existing").toMessage)
    }
  }

  "CreateFileEvent" should {
    "will broadcast to other contexts" in new Mocking {
      activeContexts(context1, context2)
      handler.channelRead(context1, createFileEvent.toMessage)
      there was one(context2).writeAndFlush(createFileEvent.toMessage)
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

    def activeContexts(contexts: ChannelHandlerContext*) {
      contexts.toList.filterNot(provider.contexts.contains).foreach(handler.channelActive)
    }

  }

  trait MockEvents {
    val changeContentEventA1 = ChangeContentEvent("/aaa", 10, "aa1", "bb1", "s1")
    val changeContentEventA1SameSummary = ChangeContentEvent("/aaa", 100, "aaaaaa1", "bbbbbbbbb1", "s1")
    val changeContentEventA2 = ChangeContentEvent("/aaa", 20, "aa2", "bb2", "s2")
    val changeContentEventB1 = ChangeContentEvent("/bbb", 30, "aa3", "bb3", "s3")
    val resetContentEvent = ResetContentEvent("/aaa", "new-content", "s4")
    val openTabEvent1 = OpenTabEvent("/aaa")
    val openTabEvent2 = OpenTabEvent("/bbb")
    val tabResetEvent = ResetTabEvent("/ccc")
    val clientInfoEvent = ClientInfoEvent("1.1.1.1", "Freewind")
    val clientInfoEvent2 = ClientInfoEvent("2.2.2.2", "Lily")
    val createFileEvent = CreateFileEvent("/aaa")
    val changeMasterEvent = ChangeMasterEvent("Lily")
  }

}
