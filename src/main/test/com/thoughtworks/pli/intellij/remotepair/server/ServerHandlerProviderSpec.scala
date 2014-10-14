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
import scala.collection.mutable

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
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeContentEventA1)

      there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
      there was no(context1).writeAndFlush(changeContentEventA1.toMessage)
    }
  }

  "Content event locks" should {
    "be added from sent ChangeContentEvent" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeContentEventA1)

      provider.contexts.get(context2) must beSome.which(_.pathSpecifiedLocks.size === 1)
    }
    "be added from ChangeContentEvent from different sources" in new Mocking {
      activeContextsWithInfo(context1, context2, context3)
      joinSameProject("test", context1, context2, context3)

      clientSendEvent(context1, changeContentEventA1) // will broadcast to other contexts as locks
      clientSendEvent(context3, changeContentEventA1) // unlock
      clientSendEvent(context3, changeContentEventA2)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.size === 1
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeContentEventA1) // will broadcast to context2 as lock
      clientSendEvent(context2, changeContentEventA1SameSummary)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(0)
      }
      there was no(context1).writeAndFlush(changeContentEventA1SameSummary.toMessage)
    }
    "send a ResetContentRequest if the feedback event is not matched for the same file path" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeContentEventA1)
      clientSendEvent(context2, changeContentEventA2)

      provider.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(1)
      }

      there was one(context1).writeAndFlush(ResetContentRequest("/aaa").toMessage)
      there was no(context2).writeAndFlush(ResetContentRequest("/aaa").toMessage)
    }
  }

  "User disconnected" should {
    "be removed from the bind groups" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      handler.channelInactive(context1)
      provider.bindModeGroups === Nil
    }
    "be removed from follower groups if its a follower" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      follow(context1, context2)

      handler.channelInactive(context1)
      provider.bindModeGroups === Nil
    }
    "be removed from follower groups if it is been followed" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      follow(context1, context2)

      handler.channelInactive(context2)
      provider.bindModeGroups === Nil
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeContentEventA1)
      clientSendEvent(context1, changeContentEventA2)
      clientSendEvent(context1, resetContentEvent)

      dataOf(context2).flatMap(_.pathSpecifiedLocks.get("/aaa")).map(_.contentLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context2, changeContentEventA1)
      clientSendEvent(context1, resetContentEvent)

      dataOf(context1).flatMap(_.pathSpecifiedLocks.get("/aaa")) must beSome.which(_.contentLocks.size === 0)
    }
  }

  "Master context" should {
    "be the first one who joined a project" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      dataOf(context1).map(_.master) === Some(true)
      dataOf(context2).map(_.master) === Some(false)
    }
    "will change to next one automatically if the master is disconnected" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      handler.channelInactive(context1)
      dataOf(context2).map(_.master) === Some(true)
    }
    "changed to the one which is requested" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, changeMasterEvent)

      dataOf(context1).map(_.master) === Some(false)
      dataOf(context2).map(_.master) === Some(true)
    }
    "response error message if specified name is not exist" in new Mocking {
      activeContextsWithInfo(context1)
      joinSameProject("test", context1)
      clientSendEvent(context1, changeMasterEvent)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }

  "OpenTabEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, openTabEvent1)
      clientSendEvent(context2, openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
      dataOf(context1).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      setMaster(context1)
      clientSendEvent(context1, openTabEvent1)
      clientSendEvent(context2, openTabEvent2)

      there was one(context1).writeAndFlush(ResetTabRequest().toMessage)
      there was no(context2).writeAndFlush(ResetTabRequest().toMessage)
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

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
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context1, moveCaretEvent3)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
      caretLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context2, moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(0)
      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetCaretRequest to master if the feedback event is not matched" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      setMaster(context1)
      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context2, moveCaretEvent2)

      there was one(context1).writeAndFlush(resetCaretRequest1.toMessage)
      there was no(context2).writeAndFlush(resetCaretRequest1.toMessage)
    }
  }

  "ResetCaretEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, moveCaretEvent1)
      clientSendEvent(context1, resetCaretEvent1)

      caretLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(15)
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)
      setMaster(context1)

      clientSendEvent(context2, moveCaretEvent1)
      clientSendEvent(context1, resetCaretEvent1)

      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "SelectContentEvent" should {
    "be a lock when it sent" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context1, selectContentEvent3)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
      selectionLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context2, selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(0)
      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetSelectionRequest to master if the feedback event is not matched" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      setMaster(context1)
      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context2, selectContentEvent2)

      there was one(context1).writeAndFlush(resetSelectionRequest.toMessage)
      there was no(context2).writeAndFlush(resetSelectionRequest.toMessage)
    }
  }

  "ResetSelectionEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      clientSendEvent(context1, selectContentEvent1)
      clientSendEvent(context1, resetSelectionEvent)

      selectionLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(SelectionRange(30, 12))
      }
    }
    "clear the master locks as well" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      bindUsers(context1, context2)

      setMaster(context1)
      clientSendEvent(context2, selectContentEvent1)
      clientSendEvent(context1, resetSelectionEvent)

      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "ClientInfoEvent" should {
    "store client name and ip to context data" in new Mocking {
      activeContextsWithInfo(context1)

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
    "broadcast to bind users" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context1, BindModeRequest("Lily"))

      clientSendEvent(context1, closeTabEvent)
      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to following users of a parallel mode user" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      clientSendEvent(context2, FollowModeRequest("Freewind"))

      clientSendEvent(context1, closeTabEvent)
      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to following users of a binding mode user" in new Mocking {
      activeContextsWithInfo(context1, context2, context3)
      joinSameProject("test", context1, context2, context3)

      clientSendEvent(context2, BindModeRequest("Freewind"))
      clientSendEvent(context3, FollowModeRequest("Freewind"))

      clientSendEvent(context1, closeTabEvent)
      there was one(context3).writeAndFlush(closeTabEvent.toMessage)
    }
  }
  "File related event" should {
    def checking(event: PairEvent) = new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

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
    "be sent automatically when there is new client joined a project" in new Mocking {
      activeContextsWithInfo(context1)
      joinSameProject("test", context1)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client updated info" in new Mocking {
      activeContextsWithInfo(context1)
      joinSameProject("test", context1)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when master changed" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      clientSendEvent(context1, ChangeMasterEvent("Lily"))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test",
          Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = false), ClientInfoData("2.2.2.2", "Lily", isMaster = true)),
          Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client disconnected" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)

      org.mockito.Mockito.reset(context1)

      handler.channelInactive(context2)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when ignored files changed" in new Mocking {
      activeContextsWithInfo(context1)
      joinSameProject("test", context1)
      clientSendEvent(context1, IgnoreFilesRequest(Seq("/aaa")))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Seq("/aaa"))),
        Nil
      ).toMessage)
    }
    "contain free clients" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Seq(ClientInfoData("2.2.2.2", "Lily", isMaster = false))
      ).toMessage)
    }
  }

  "IgnoreFilesRequest" should {
    "store the files on server" in new Mocking {
      activeContextsWithInfo(context1)
      joinSameProject("test", context1)
      clientSendEvent(context1, IgnoreFilesRequest(Seq("/aaa", "/bbb")))
      provider.projects("test").ignoredFiles === Seq("/aaa", "/bbb")
    }
  }

  "SyncFilesRequest" should {
    "forward to master" in new Mocking {
      activeContextsWithInfo(context1, context2)
      joinSameProject("test", context1, context2)
      setMaster(context2)
      clientSendEvent(context1, syncFilesRequest)
      there was one(context2).writeAndFlush(syncFilesRequest.toMessage)
      there was no(context1).writeAndFlush(syncFilesRequest.toMessage)
    }
  }

  "mode related requests" should {
    "BindModeRequest" should {
      "bind with the specified client" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)

        clientSendEvent(context1, BindModeRequest("Lily"))
        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily")): _*))
      }
      "allow client bind to an existing group" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, BindModeRequest("Lily"))
        clientSendEvent(context3, BindModeRequest("Lily"))

        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily", "Mike")): _*))
      }
      "allow different groups with different clients" in new Mocking {
        activeContextsWithInfo(context1, context2, context3, context4, context5)
        joinSameProject("test", context1, context2, context3, context4, context5)

        clientSendEvent(context1, BindModeRequest("Lily"))
        clientSendEvent(context3, BindModeRequest("Jeff"))
        clientSendEvent(context5, BindModeRequest("Jeff"))

        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily"), Set("Mike", "Jeff", "Alex")): _*))
      }
      "allow changing to bind another client" in new Mocking {
        activeContextsWithInfo(context1, context2, context3, context4, context5)
        joinSameProject("test", context1, context2, context3, context4, context5)

        clientSendEvent(context1, BindModeRequest("Lily"))
        clientSendEvent(context3, BindModeRequest("Lily"))
        clientSendEvent(context5, BindModeRequest("Jeff"))
        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily", "Mike"), Set("Jeff", "Alex")): _*))

        clientSendEvent(context3, BindModeRequest("Jeff"))
        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily"), Set("Mike", "Jeff", "Alex")): _*))
      }
      "change the mode of client from other mode" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context1, BindModeRequest("Mike"))

        provider.bindModeGroups === List(Set("Mike", "Freewind"))
        provider.followModeMap must beEmpty
      }
      "change the mode of target client to BindMode as well" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2)

        clientSendEvent(context2, FollowModeRequest("Mike"))
        clientSendEvent(context1, BindModeRequest("Lily"))

        provider.bindModeGroups === List(Set("Lily", "Freewind"))
        provider.followModeMap must beEmpty
      }
      "not bind to self" in new Mocking {
        activeContextsWithInfo(context1)
        joinSameProject("test", context1)

        clientSendEvent(context1, BindModeRequest("Freewind"))
        provider.bindModeGroups === Nil
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't bind to self").toMessage)
      }
      "not bind to a non-exist client" in new Mocking {
        activeContextsWithInfo(context1)
        joinSameProject("test", context1, context2)

        clientSendEvent(context1, BindModeRequest("non-exist-user"))
        provider.bindModeGroups === Nil
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't bind to non-exist user: 'non-exist-user'").toMessage)
      }
      "not do anything if they are already in the same group" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)

        clientSendEvent(context1, BindModeRequest("Lily"))
        clientSendEvent(context1, BindModeRequest("Lily"))

        provider.bindModeGroups must contain(exactly(List(Set("Freewind", "Lily")): _*))
      }
      "broadcast tab events with each other" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)
        clientSendEvent(context1, BindModeRequest("Lily"))

        clientSendEvent(context1, openTabEvent1)
        clientSendEvent(context1, closeTabEvent)
        clientSendEvent(context1, resetTabEvent)

        there was one(context2).writeAndFlush(openTabEvent1.toMessage)
        there was one(context2).writeAndFlush(closeTabEvent.toMessage)
        there was one(context2).writeAndFlush(resetTabEvent.toMessage)
      }
      "broadcast caret events with each other" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)
        clientSendEvent(context1, BindModeRequest("Lily"))

        clientSendEvent(context1, moveCaretEvent1)
        clientSendEvent(context1, resetCaretEvent1)

        there was one(context2).writeAndFlush(moveCaretEvent1.toMessage)
        there was one(context2).writeAndFlush(resetCaretEvent1.toMessage)
      }
      "broadcast selection events with each other" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)
        clientSendEvent(context1, BindModeRequest("Lily"))

        clientSendEvent(context1, selectContentEvent1)
        clientSendEvent(context1, resetSelectionEvent)

        there was one(context2).writeAndFlush(selectContentEvent1.toMessage)
        there was one(context2).writeAndFlush(resetSelectionEvent.toMessage)
      }
      "broadcast content events with each other" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)
        clientSendEvent(context1, BindModeRequest("Lily"))

        clientSendEvent(context1, changeContentEventA1)
        clientSendEvent(context1, resetContentEvent)

        there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
        there was one(context2).writeAndFlush(resetContentEvent.toMessage)
      }
      "send info to impacted users" in new Mocking {
        // need to find a proper way to notify them
        todo
      }
      "can't bind others if it's not in any group" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context2)

        clientSendEvent(context1, BindModeRequest("Lily"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
      }
      "can't bind user who is not in the same project" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test1", context1)
        joinSameProject("test2", context2)

        clientSendEvent(context1, BindModeRequest("Lily"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Operation is failed because 'Lily' is not in the same project").toMessage)
      }
    }
    "ParallelModeRequest" should {
      "change the mode of client from other mode" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)
        clientSendEvent(context1, BindModeRequest("Lily"))

        clientSendEvent(context1, ParallelModeRequest())
        provider.bindModeGroups === Nil
      }
      "only broadcast tab events to followers" in new Mocking {
        sendToFollowers(openTabEvent1, closeTabEvent)
      }
      "only broadcast caret events to followers" in new Mocking {
        sendToFollowers(moveCaretEvent1, resetCaretEvent1)
      }
      "only broadcast selection events to followers" in new Mocking {
        sendToFollowers(selectContentEvent1, resetSelectionEvent)
      }

      def sendToFollowers(events: PairEvent*) = new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)
        clientSendEvent(context2, FollowModeRequest("Freewind"))

        events.foreach { event =>
          clientSendEvent(context1, event)
          there was one(context2).writeAndFlush(event.toMessage)
          there was no(context3).writeAndFlush(event.toMessage)
        }
      }
    }
    "FollowModeRequest" should {
      "follow other client" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)

        clientSendEvent(context1, FollowModeRequest("Lily"))

        provider.followModeMap must havePair("Lily" -> Set("Freewind"))
      }
      "mutli users follow one same user" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context3, FollowModeRequest("Lily"))

        provider.followModeMap must havePair("Lily" -> Set("Freewind", "Mike"))
      }
      "not follow self" in new Mocking {
        activeContextsWithInfo(context1)
        joinSameProject("test", context1)

        clientSendEvent(context1, FollowModeRequest("Freewind"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow self").toMessage)
        provider.followModeMap must beEmpty
      }
      "not follow non-exist user" in new Mocking {
        activeContextsWithInfo(context1)
        joinSameProject("test", context1)

        clientSendEvent(context1, FollowModeRequest("non-exist-user"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow non-exist user: 'non-exist-user'").toMessage)
        provider.followModeMap must beEmpty
      }
      "not follow the follower" in new Mocking {
        activeContextsWithInfo(context1, context2)
        joinSameProject("test", context1, context2)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context2, FollowModeRequest("Freewind"))

        there was one(context2).writeAndFlush(ServerErrorResponse("Can't follow your follower: 'Freewind'").toMessage)
        provider.followModeMap must havePair("Lily" -> Set("Freewind"))
      }
      "able to change the target user" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context1, FollowModeRequest("Mike"))

        provider.followModeMap === Map("Mike" -> Set("Freewind"))
      }
      "follow the target of a follow" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context3, FollowModeRequest("Freewind"))

        provider.followModeMap === Map("Lily" -> Set("Freewind", "Mike"))
      }
      "take all the followers to follow new target" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, FollowModeRequest("Lily"))
        clientSendEvent(context2, FollowModeRequest("Mike"))

        provider.followModeMap === Map("Mike" -> Set("Freewind", "Lily"))
      }
      "change the mode of client from other mode" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        clientSendEvent(context1, BindModeRequest("Lily"))
        clientSendEvent(context1, FollowModeRequest("Mike"))
        provider.followModeMap === Map("Mike" -> Set("Freewind"))
        provider.bindModeGroups === Nil
      }
      "not broadcast content events to others" in new Mocking {
        notBroadcastToOthers(changeContentEventA1, resetContentEvent)
      }
      "not broadcast tab events to others" in new Mocking {
        notBroadcastToOthers(openTabEvent1, closeTabEvent, resetTabEvent)
      }
      "not broadcast caret events to others" in new Mocking {
        notBroadcastToOthers(moveCaretEvent1, resetCaretEvent1)
      }
      "not broadcast selection events to others" in new Mocking {
        notBroadcastToOthers(selectContentEvent1, resetSelectionEvent)
      }
      "not broadcast file events to others" in new Mocking {
        notBroadcastToOthers(createFileEvent, deleteFileEvent, createDirEvent, deleteDirEvent, renameEvent)
      }

      def notBroadcastToOthers(events: PairEvent*) = new Mocking {
        activeContextsWithInfo(context1, context2, context3)
        joinSameProject("test", context1, context2, context3)

        follow(context1, context2)

        events.foreach { event =>
          clientSendEvent(context1, event)
          there was no(context2).writeAndFlush(event.toMessage)
          there was no(context3).writeAndFlush(event.toMessage)
        }
      }
    }
  }

  "Project request from client" should {
    "CreateProjectRequest" should {
      "not be sent by user who has not sent ClientInfoEvent" in new Mocking {
        activeContexts(context1)
        clientSendEvent(context1, CreateProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
      }
      "create a new project and join it with a new name on the server" in new Mocking {
        activeContextsWithInfo(context1)

        clientSendEvent(context1, CreateProjectRequest("test"))
        provider.projects must haveProjectMembers("test", Set("Freewind"))
      }
      "not create a project with existing name" in new Mocking {
        activeContextsWithInfo(context1)
        clientSendEvent(context1, CreateProjectRequest("test"))
        clientSendEvent(context1, CreateProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Project 'test' is already exist, can't create again").toMessage)
      }
    }
    "JoinProjectRequest" should {
      "not be sent by user who has not sent ClientInfoEvent" in new Mocking {
        activeContexts(context1)
        clientSendEvent(context1, JoinProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
      }
      "join an existing project" in new Mocking {
        activeContextsWithInfo(context1, context2)
        clientSendEvent(context1, CreateProjectRequest("test"))
        clientSendEvent(context2, JoinProjectRequest("test"))
        provider.projects must haveProjectMembers("test", Set("Freewind", "Lily"))
      }
      "not join an non-exist project" in new Mocking {
        activeContextsWithInfo(context1)
        clientSendEvent(context1, JoinProjectRequest("non-exist"))
        there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'non-exist'").toMessage)
      }
      "leave original project when join another" in new Mocking {
        activeContextsWithInfo(context1, context2)
        clientSendEvent(context1, CreateProjectRequest("test1"))
        clientSendEvent(context2, CreateProjectRequest("test2"))
        clientSendEvent(context1, JoinProjectRequest("test2"))
        provider.projects must haveProjectMembers("test2", Set("Freewind", "Lily"))
      }
    }
    "Project on server" should {
      "be destroyed if no one joined" in new Mocking {
        activeContextsWithInfo(context1)
        clientSendEvent(context1, CreateProjectRequest("test1"))
        clientSendEvent(context1, CreateProjectRequest("test2"))
        provider.projects must haveSize(1)
        provider.projects must haveProjectMembers("test2", Set("Freewind"))
      }
    }
    "User who has not joined to any project" should {
      "able to receive ServerStatusResponse" in new Mocking {
        activeContextsWithInfo(context1, context2)
        there was one(context1).writeAndFlush(ServerStatusResponse(
          Nil,
          Seq(ClientInfoData("1.1.1.1", "Freewind", isMaster = false), ClientInfoData("2.2.2.2", "Lily", isMaster = false))
        ).toMessage)
      }
      "able to receive ServerErrorResponse" in new Mocking {
        activeContextsWithInfo(context1, context2)
        clientSendEvent(context1, JoinProjectRequest("any"))
        there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'any'").toMessage)
      }
      "not send editor related events" in new Mocking {
        cantSendEvents(
          openTabEvent1, closeTabEvent, resetTabEvent,
          changeContentEventA1, resetContentEvent,
          moveCaretEvent1, resetCaretEvent1,
          selectContentEvent1, resetSelectionEvent
        )
      }
      "not send mode related request" in new Mocking {
        cantSendEvents(
          FollowModeRequest("Lily"),
          BindModeRequest("Lily"),
          ParallelModeRequest()
        )
      }
      "not send master related request" in new Mocking {
        cantSendEvents(changeContentEventA1)
      }
      "not send IgnoreFilesRequest related request" in new Mocking {
        cantSendEvents(IgnoreFilesRequest(Seq("/aaa")))
      }
      "not send SyncFilesRequest related request" in new Mocking {
        cantSendEvents(syncFilesRequest)
      }
      def cantSendEvents(events: PairEvent*) = new Mocking {
        activeContexts(context1)
        events.foreach(event => clientSendEvent(context1, event))

        there were atLeast(events.size)(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
      }
    }
    "User who has joined to a project" should {
      "only receive events from users in the same project" in new Mocking {
        activeContextsWithInfo(context1, context2, context3)

        clientSendEvent(context1, CreateProjectRequest("p1"))
        clientSendEvent(context2, JoinProjectRequest("p1"))
        clientSendEvent(context3, CreateProjectRequest("p3"))

        clientSendEvent(context1, changeContentEventA1)

        there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
        there was no(context3).writeAndFlush(changeContentEventA1.toMessage)
      }
    }
  }

  def haveProjectMembers(projectName: String, members: Set[String]) =
    beSome[Project].which(_.members == members) ^^ { (x: Map[String, Project]) => x.get(projectName)}

  trait Mocking extends Scope with MockEvents {
    m =>

    private val contexts = mutable.LinkedHashMap.empty[ChannelHandlerContext, ContextData]
    private var projects = Map.empty[String, Project]
    private var bindModeGroups = List.empty[Set[String]]
    private var followModeMap = Map.empty[String, Set[String]]

    trait Singletons extends ClientModeGroups with ProjectsHolder with ContextHolder {
      def contexts = new Contexts {
        override val contexts = m.contexts
      }

      def projects = m.projects

      def projects_=(projects: Map[String, Project]) = m.projects = projects

      def bindModeGroups = m.bindModeGroups

      def bindModeGroups_=(groups: List[Set[String]]) = m.bindModeGroups = groups

      def followModeMap = m.followModeMap

      def followModeMap_=(map: Map[String, Set[String]]) = m.followModeMap = map
    }

    val provider = new ServerHandlerProvider with Singletons

    def dataOf(context: ChannelHandlerContext) = {
      provider.contexts.get(context)
    }

    def setMaster(context: ChannelHandlerContext) {
      if (!provider.contexts.contains(context)) {
        provider.contexts.add(context)
      }
      provider.contexts.all.foreach(_.master = false)
      dataOf(context).foreach(_.master = true)
    }

    val handler = provider.createServerHandler()
    val context1 = mock[ChannelHandlerContext]
    val context2 = mock[ChannelHandlerContext]
    val context3 = mock[ChannelHandlerContext]
    val context4 = mock[ChannelHandlerContext]
    val context5 = mock[ChannelHandlerContext]

    val contextWithInfo = Map(
      context1 -> clientInfoEvent1,
      context2 -> clientInfoEvent2,
      context3 -> clientInfoEvent3,
      context4 -> clientInfoEvent4,
      context5 -> clientInfoEvent5
    )

    def activeContexts(contexts: ChannelHandlerContext*) {
      contexts.toList.filterNot(provider.contexts.contains).foreach(handler.channelActive)
    }

    def activeContextsWithInfo(contexts: ChannelHandlerContext*) {
      contexts.toList.filterNot(provider.contexts.contains).foreach { ctx =>
        handler.channelActive(ctx)
        clientSendEvent(ctx, contextWithInfo(ctx))
      }
    }

    def joinSameProject(projectName: String, contexts: ChannelHandlerContext*) {
      clientSendEvent(contexts.head, CreateProjectRequest(projectName))
      contexts.foreach(ctx => clientSendEvent(ctx, JoinProjectRequest(projectName)))
    }

    def follow(context1: ChannelHandlerContext, context2: ChannelHandlerContext) {
      clientSendEvent(context1, FollowModeRequest(dataOf(context2).get.name))
    }

    def bindUsers(contexts: ChannelHandlerContext*) {
      val first = contexts.head
      contexts.tail.foreach(c => clientSendEvent(first, BindModeRequest(dataOf(c).get.name)))
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

    val clientInfoEvent1 = ClientInfoEvent("1.1.1.1", "Freewind")
    val clientInfoEvent2 = ClientInfoEvent("2.2.2.2", "Lily")
    val clientInfoEvent3 = ClientInfoEvent("3.3.3.3", "Mike")
    val clientInfoEvent4 = ClientInfoEvent("4.4.4.4", "Jeff")
    val clientInfoEvent5 = ClientInfoEvent("5.5.5.5", "Alex")

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

    val syncFilesRequest = SyncFilesRequest()
  }

}
