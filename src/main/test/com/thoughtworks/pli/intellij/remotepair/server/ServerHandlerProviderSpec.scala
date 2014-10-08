package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import io.netty.channel.ChannelHandlerContext
import org.specs2.mock.Mockito
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
      clientSendEvent(context1, changeContentEventA1)

      there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
      there was no(context1).writeAndFlush(changeContentEventA1.toMessage)
    }
  }

  "Content event locks" should {
    "be added from sent ChangeContentEvent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, changeContentEventA1)

      provider.contexts.get(context2) must beSome.which(_.pathSpecifiedLocks.size === 1)
    }
    "be added from ChangeContentEvent from different sources" in new Mocking {
      activeContexts(context1, context2, context3)
      clientSendEvent(context1, changeContentEventA1) // will broadcast to other contexts as locks
      clientSendEvent(context3, changeContentEventA1) // unlock
      clientSendEvent(context3, changeContentEventA2)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.size === 1
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, changeContentEventA1) // will broadcast to context2 as lock
      clientSendEvent(context2, changeContentEventA1SameSummary)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(0)
      }
      there was no(context1).writeAndFlush(changeContentEventA1SameSummary.toMessage)
    }
    "send a ResetContentRequest if the feedback event is not matched for the same file path" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, changeContentEventA1)
      clientSendEvent(context2, changeContentEventA2)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(1)
      }

      there was one(context1).writeAndFlush(ResetContentRequest("/aaa").toMessage)
      there was no(context2).writeAndFlush(ResetContentRequest("/aaa").toMessage)
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, changeContentEventA1)
      clientSendEvent(context1, changeContentEventA2)
      clientSendEvent(context1, resetContentEvent)

      dataOf(context2).flatMap(_.pathSpecifiedLocks.get("/aaa")).map(_.contentLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context2, changeContentEventA1)
      clientSendEvent(context1, resetContentEvent)

      dataOf(context1).flatMap(_.pathSpecifiedLocks.get("/aaa")) must beSome.which(_.contentLocks.size === 0)
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
      clientSendEvent(context2, clientInfoEvent2)

      clientSendEvent(context1, changeMasterEvent)

      dataOf(context1).map(_.master) === Some(false)
      dataOf(context2).map(_.master) === Some(true)
    }
    "response error message if specified name is not exist" in new Mocking {
      handler.channelActive(context1)
      clientSendEvent(context1, clientInfoEvent)
      clientSendEvent(context1, changeMasterEvent)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }

  "OpenTabEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, openTabEvent1)
      clientSendEvent(context2, openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
      dataOf(context1).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context1, openTabEvent1)
      clientSendEvent(context2, openTabEvent2)

      there was one(context1).writeAndFlush(ResetTabRequest().toMessage)
      there was no(context2).writeAndFlush(ResetTabRequest().toMessage)
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, openTabEvent1)
      clientSendEvent(context1, resetTabEvent)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some("/ccc")
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context2, openTabEvent1)
      clientSendEvent(context1, resetTabEvent)

      dataOf(context1).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
    }
  }

  "MoveCaretEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context1, moveCaretEvent3)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
      caretLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context2, moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(0)
      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetCaretRequest to master if the feedback event is not matched" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context2, moveCaretEvent2)

      there was one(context1).writeAndFlush(resetCaretRequest1.toMessage)
      there was no(context2).writeAndFlush(resetCaretRequest1.toMessage)
    }
  }

  "ResetCaretEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context1, resetCaretEvent1)

      caretLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(15)
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context2, moveCaretEvent1)
      clientSendEvent(context1, resetCaretEvent1)

      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "SelectContentEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context1, selectContentEvent3)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
      selectionLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context2, selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(0)
      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetSelectionRequest to master if the feedback event is not matched" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context2, selectContentEvent2)

      there was one(context1).writeAndFlush(resetSelectionRequest.toMessage)
      there was no(context2).writeAndFlush(resetSelectionRequest.toMessage)
    }
  }

  "ResetSelectionEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context1, resetSelectionEvent)

      selectionLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(SelectionRange(30, 12))
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContexts(context1, context2)
      setMaster(context1)
      clientSendEvent(context2, selectContentEvent1)
      clientSendEvent(context1, resetSelectionEvent)

      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "ClientInfoEvent" should {
    "store client name and ip to context data" in new Mocking {
      activeContexts(context1)
      clientSendEvent(context1, clientInfoEvent)

      dataOf(context1).map(_.name) === Some("Freewind")
      dataOf(context1).map(_.ip) === Some("1.1.1.1")
    }
    "get an error back if the name is blank" in new Mocking {
      activeContexts(context1)
      clientSendEvent(context1, ClientInfoEvent("non-empty-ip", "  "))
      there was one(context1).writeAndFlush(ServerErrorResponse("Name is not provided").toMessage)
    }
    "get an error back if the name is already existing" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, ClientInfoEvent("non-empty-ip", "Freewind"))
      clientSendEvent(context2, ClientInfoEvent("non-empty-ip", "Freewind"))
      there was one(context2).writeAndFlush(ServerErrorResponse("Specified name 'Freewind' is already existing").toMessage)
    }
  }

  "CloseTabEvent" should {
    "broadcast to other contexts" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, closeTabEvent)
      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
  }
  "File related event" should {
    def checking(event: PairEvent) = new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context1, event)
      clientSendEvent(context1, event)
      there was two(context2).writeAndFlush(event.toMessage)
    }
    "broadcast to other contexts for CreateFileEvent" in new Mocking {
      checking(createFileEvent)
    }
    "broadcast to other contexts for DeleteFileEvent" in new Mocking {
      checking(deleteFileEvent)
    }
    "broadcast to other contexts for CreateDirEvent" in new Mocking {
      checking(createDirEvent)
    }
    "broadcast to other contexts for DeleteDirEvent" in new Mocking {
      checking(deleteDirEvent)
    }
    "broadcast to other contexts for RenameEvent" in new Mocking {
      checking(renameEvent)
    }
  }

  "ServerStatusResponse" should {
    "be sent automatically when there is new client connected" in new Mocking {
      handler.channelActive(context1)
      there was one(context1).writeAndFlush(ServerStatusResponse(Seq(
        ClientInfoData("Unknown", "Unknown", isMaster = true)
      )).toMessage)
    }
    "be sent automatically when client updated info" in new Mocking {
      handler.channelActive(context1)
      clientSendEvent(context1, ClientInfoEvent("test-ip", "test-name"))
      there was one(context1).writeAndFlush(ServerStatusResponse(Seq(
        ClientInfoData("test-ip", "test-name", isMaster = true)
      )).toMessage)
    }
    "be sent automatically when master changed" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context2, ClientInfoEvent("test-ip", "Freewind"))
      clientSendEvent(context1, ChangeMasterEvent("Freewind"))
      there was one(context1).writeAndFlush(ServerStatusResponse(Seq(
        ClientInfoData("Unknown", "Unknown", isMaster = false),
        ClientInfoData("test-ip", "Freewind", isMaster = true)
      )).toMessage)
    }
    "be sent automatically when client disconnected" in new Mocking {
      activeContexts(context1, context2)
      clientSendEvent(context2, ClientInfoEvent("test-ip", "test-name"))
      handler.channelInactive(context1)
      there was one(context2).writeAndFlush(ServerStatusResponse(Seq(
        ClientInfoData("test-ip", "test-name", isMaster = true)
      )).toMessage)
    }
  }


  trait Mocking extends Scope with MockEvents {

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
    val context1 = mock[ChannelHandlerContext]
    val context2 = mock[ChannelHandlerContext]
    val context3 = mock[ChannelHandlerContext]

    def activeContexts(contexts: ChannelHandlerContext*) {
      contexts.toList.filterNot(provider.contexts.contains).foreach(handler.channelActive)
    }

    def clientSendEvent(context: ChannelHandlerContext, event: PairEvent) {
      handler.channelRead(context, event.toMessage)
    }

    def caretLock(context: ChannelHandlerContext, path: String) = {
      dataOf(context).flatMap(_.pathSpecifiedLocks.get(path)).map(_.caretLocks)
    }

    def selectionLock(context: ChannelHandlerContext, path: String) = {
      dataOf(context).flatMap(_.pathSpecifiedLocks.get(path)).map(_.selectionLocks)
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
    val closeTabEvent = CloseTabEvent("/aaa")
    val resetTabEvent = ResetTabEvent("/ccc")
    val clientInfoEvent = ClientInfoEvent("1.1.1.1", "Freewind")
    val clientInfoEvent2 = ClientInfoEvent("2.2.2.2", "Lily")
    val createFileEvent = CreateFileEvent("/aaa")
    val deleteFileEvent = DeleteFileEvent("/aaa")
    val createDirEvent = CreateFileEvent("/ddd")
    val deleteDirEvent = DeleteFileEvent("/ddd")
    val renameEvent = RenameEvent("/ccc", "/eee")
    val changeMasterEvent = ChangeMasterEvent("Lily")

    val moveCaretEvent1 = MoveCaretEvent("/aaa", 10)
    val moveCaretEvent2 = MoveCaretEvent("/aaa", 20)
    val moveCaretEvent3 = MoveCaretEvent("/bbb", 10)
    val resetCaretRequest1 = ResetCaretRequest("/aaa")
    val resetCaretEvent1 = ResetCaretEvent("/aaa", 15)

    val selectContentEvent1 = SelectContentEvent("/aaa", 10, 5)
    val selectContentEvent2 = SelectContentEvent("/aaa", 20, 7)
    val selectContentEvent3 = SelectContentEvent("/bbb", 14, 8)
    val resetSelectionRequest = ResetSelectionRequest("/aaa")
    val resetSelectionEvent = ResetSelectionEvent("/aaa", 30, 12)

  }

}
