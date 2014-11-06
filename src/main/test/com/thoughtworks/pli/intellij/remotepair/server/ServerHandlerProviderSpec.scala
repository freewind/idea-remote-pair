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

  "When client is connected, server" should {
    "ask for client information" in new Mocking {
      handler.channelActive(context1)
      there was one(context1).writeAndFlush(AskForClientInformation().toMessage)
    }
  }

  "ServerHandler" should {
    "add the context to global cache when channelActive" in new Mocking {
      handler.channelActive(context1)
      handler.contexts.size === 1
    }
    "remove the context from global cache when channel is inactive" in new Mocking {
      handler.contexts.add(context1)
      handler.channelInactive(context1)
      handler.contexts.size === 0
    }
    "can only handle ByteBuf message type" in new Mocking {
      handler.contexts.add(context1)
      handler.contexts.add(context2)

      handler.channelRead(context1, "unknown-message-type")

      there was no(context2).writeAndFlush(any)
    }
    "broadcast received event to other context" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1)

      there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
      there was no(context1).writeAndFlush(changeContentEventA1.toMessage)
    }
  }

  "Content event locks" should {
    "be added from sent ChangeContentEvent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1)

      handler.contexts.get(context2) must beSome.which(_.pathSpecifiedLocks.size === 1)
    }
    "be added from ChangeContentEvent from different sources" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1) // will broadcast to other contexts as locks
      client(context3).send(changeContentEventA1) // unlock
      client(context3).send(changeContentEventA2)

      handler.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.size === 1
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1) // will broadcast to context2 as lock
      client(context2).send(changeContentEventA1SameSummary)

      handler.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(0)
      }
      there was no(context1).writeAndFlush(changeContentEventA1SameSummary.toMessage)
    }
    "send a ResetContentRequest if the feedback event is not matched for the same file path" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1)
      client(context2).send(changeContentEventA2)

      handler.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(1)
      }

      there was one(context1).writeAndFlush(ResetContentRequest("/aaa").toMessage)
      there was no(context2).writeAndFlush(ResetContentRequest("/aaa").toMessage)
    }
  }

  "User disconnected" should {
    "be removed from the bind groups" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      handler.channelInactive(context1)
      handler.caretSharingModeGroups === Nil
    }
    "be removed from follower groups if its a follower" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).follow(context2)

      handler.channelInactive(context1)
      handler.caretSharingModeGroups === Nil
    }
    "be removed from follower groups if it is been followed" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).follow(context2)

      handler.channelInactive(context2)
      handler.caretSharingModeGroups === Nil
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1, changeContentEventA2, resetContentEvent)

      dataOf(context2).flatMap(_.pathSpecifiedLocks.get("/aaa")).map(_.contentLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context2).send(changeContentEventA1)
      client(context1).send(resetContentEvent)

      dataOf(context1).flatMap(_.pathSpecifiedLocks.get("/aaa")) must beSome.which(_.contentLocks.size === 0)
    }
  }

  "Master context" should {
    "be the first one who joined a project" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      dataOf(context1).map(_.master) === Some(true)
      dataOf(context2).map(_.master) === Some(false)
    }
    "will change to next one automatically if the master is disconnected" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      handler.channelInactive(context1)
      dataOf(context2).map(_.master) === Some(true)
    }
    "changed to the one which is requested" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeMasterEvent)

      dataOf(context1).map(_.master) === Some(false)
      dataOf(context2).map(_.master) === Some(true)
    }
    "response error message if specified name is not exist" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test").send(changeMasterEvent)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }

  "OpenTabEvent" should {
    "be a lock when it sent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(openTabEvent1)
      client(context2).send(openTabEvent1)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
      dataOf(context1).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster().send(openTabEvent1)
      client(context2).send(openTabEvent2)

      there was one(context1).writeAndFlush(ResetTabRequest().toMessage)
      there was no(context2).writeAndFlush(ResetTabRequest().toMessage)
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(openTabEvent1, resetTabEvent)

      dataOf(context2).map(_.projectSpecifiedLocks.activeTabLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some("/ccc")
      }
    }
    "clear the master locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = false)
      client(context1).beMaster()

      client(context2).send(openTabEvent1)
      client(context1).send(resetTabEvent)

      dataOf(context1).map(_.projectSpecifiedLocks.activeTabLocks.size) === Some(0)
    }
  }

  "MoveCaretEvent" should {
    "be a lock when it sent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1, moveCaretEvent3)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
      caretLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1)
      client(context2).send(moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(0)
      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetCaretRequest to master if the feedback event is not matched" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster().send(moveCaretEvent1)
      client(context2).send(moveCaretEvent2)

      there was one(context1).writeAndFlush(resetCaretRequest1.toMessage)
      there was no(context2).writeAndFlush(resetCaretRequest1.toMessage)
    }
  }

  "ResetCaretEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1, resetCaretEvent1)

      caretLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(15)
      }
    }
    "clear the master locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()
      client(context1).beMaster()

      client(context2).send(moveCaretEvent1)
      client(context1).send(resetCaretEvent1)

      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "SelectContentEvent" should {
    "be a lock when it sent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1, selectContentEvent3)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
      selectionLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(selectContentEvent1)
      client(context2).send(selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(0)
      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetSelectionRequest to master if the feedback event is not matched" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster().send(selectContentEvent1)
      client(context2).send(selectContentEvent2)

      there was one(context1).writeAndFlush(resetSelectionRequest.toMessage)
      there was no(context2).writeAndFlush(resetSelectionRequest.toMessage)
    }
  }

  "ResetSelectionEvent" should {
    "clear existing locks and be the new lock" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1, resetSelectionEvent)

      selectionLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(SelectionRange(30, 12))
      }
    }
    "clear the master locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster()
      client(context2).send(selectContentEvent1)
      client(context1).send(resetSelectionEvent)

      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

  "ClientInfoEvent" should {
    "store client name and ip to context data" in new Mocking {
      client(context1).active(sendInfo = true)

      dataOf(context1).map(_.name) === Some("Freewind")
      dataOf(context1).map(_.ip) === Some("1.1.1.1")
    }
    "get an error back if the name is blank, and ask for information again" in new Mocking {
      client(context1).active(sendInfo = false)
      org.mockito.Mockito.reset(context1)

      client(context1).send(ClientInfoEvent("non-empty-ip", "  "))
      there was one(context1).writeAndFlush(ServerErrorResponse("Name is not provided").toMessage)
      there was one(context1).writeAndFlush(AskForClientInformation().toMessage)
    }
    "get an error back if the name is already existing, and ask for information again" in new Mocking {
      client(context1, context2).active(sendInfo = false)
      org.mockito.Mockito.reset(context2)

      client(context1).send(ClientInfoEvent("non-empty-ip", "Freewind"))
      client(context2).send(ClientInfoEvent("non-empty-ip", "Freewind"))
      there was one(context2).writeAndFlush(ServerErrorResponse("Specified name 'Freewind' is already existing").toMessage)
      there was one(context2).writeAndFlush(AskForClientInformation().toMessage)
    }
  }

  "AskForJoinProject" should {
    "send to client if has gotten client's information" in new Mocking {
      client(context1).active(sendInfo = true)
      there was one(context1).writeAndFlush(AskForJoinProject().toMessage)
    }
  }

  "AskForWorkingMode" should {
    "send to client if has gotten client's information and project chosen" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was one(context1).writeAndFlush(AskForWorkingMode().toMessage)
    }
  }

  "CloseTabEvent" should {
    "broadcast to bind users" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(CaretSharingModeRequest("Lily"), closeTabEvent)

      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to following users of a parallel mode user" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context2).follow("Freewind")

      client(context1).send(closeTabEvent)
      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to following users of a binding mode user" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context2).send(CaretSharingModeRequest("Freewind"))
      client(context3).follow("Freewind")

      client(context1).send(closeTabEvent)
      there was one(context3).writeAndFlush(closeTabEvent.toMessage)
    }
  }
  "File related event" should {
    def checking(event: PairEvent) = new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(event, event)

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
      client(context1).active(sendInfo = true).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client updated info" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when master changed" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).send(ChangeMasterEvent("Lily"))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test",
          Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = false), ClientInfoData("2.2.2.2", "Lily", isMaster = true)),
          Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client disconnected" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      org.mockito.Mockito.reset(context1)

      handler.channelInactive(context2)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when ignored files changed" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).send(IgnoreFilesRequest(Seq("/aaa")))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Seq("/aaa"))),
        Nil
      ).toMessage)
    }
    "contain free clients" in new Mocking {
      client(context1, context2).active(sendInfo = true)
      client(context1).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Set(ClientInfoData("1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Seq(ClientInfoData("2.2.2.2", "Lily", isMaster = false))
      ).toMessage)
    }
  }

  "IgnoreFilesRequest" should {
    "store the files on server" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test").send(IgnoreFilesRequest(Seq("/aaa", "/bbb")))

      handler.projects("test").ignoredFiles === Seq("/aaa", "/bbb")
    }
  }

  "SyncFilesRequest" should {
    "forward to master" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context2).beMaster()
      client(context1).send(syncFilesRequest)
      there was one(context2).writeAndFlush(syncFilesRequest.toMessage)
      there was no(context1).writeAndFlush(syncFilesRequest.toMessage)
    }
  }

  "mode related requests" should {
    "BindModeRequest" should {
      "bind with the specified client" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"))
        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily")): _*))
      }
      "allow client bind to an existing group" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"))
        client(context3).send(CaretSharingModeRequest("Lily"))

        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily", "Mike")): _*))
      }
      "allow different groups with different clients" in new Mocking {
        client(context1, context2, context3, context4, context5).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"))
        client(context3).send(CaretSharingModeRequest("Jeff"))
        client(context5).send(CaretSharingModeRequest("Jeff"))

        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily"), Set("Mike", "Jeff", "Alex")): _*))
      }
      "allow changing to bind another client" in new Mocking {
        client(context1, context2, context3, context4, context5).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"))
        client(context3).send(CaretSharingModeRequest("Lily"))
        client(context5).send(CaretSharingModeRequest("Jeff"))
        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily", "Mike"), Set("Jeff", "Alex")): _*))

        client(context3).send(CaretSharingModeRequest("Jeff"))
        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily"), Set("Mike", "Jeff", "Alex")): _*))
      }
      "change the mode of client from other mode" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily")).send(CaretSharingModeRequest("Mike"))

        handler.caretSharingModeGroups === List(Set("Mike", "Freewind"))
        handler.followModeMap must beEmpty
      }
      "change the mode of target client to BindMode as well" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true)
        client(context1, context2).joinProject("test")

        client(context2).send(FollowModeRequest("Mike"))
        client(context1).send(CaretSharingModeRequest("Lily"))

        handler.caretSharingModeGroups === List(Set("Lily", "Freewind"))
        handler.followModeMap must beEmpty
      }
      "not bind to self" in new Mocking {
        client(context1).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Freewind"))
        handler.caretSharingModeGroups === Nil
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't bind to self").toMessage)
      }
      "not bind to a non-exist client" in new Mocking {
        client(context1).active(sendInfo = true)
        client(context1, context2).joinProject("test")

        client(context1).send(CaretSharingModeRequest("non-exist-user"))
        handler.caretSharingModeGroups === Nil
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't bind to non-exist user: 'non-exist-user'").toMessage)
      }
      "not do anything if they are already in the same group" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"), CaretSharingModeRequest("Lily"))

        handler.caretSharingModeGroups must contain(exactly(List(Set("Freewind", "Lily")): _*))
      }
      "broadcast tab events with each other" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")
        client(context1).send(CaretSharingModeRequest("Lily"))

        client(context1).send(openTabEvent1, closeTabEvent, resetTabEvent)

        there was one(context2).writeAndFlush(openTabEvent1.toMessage)
        there was one(context2).writeAndFlush(closeTabEvent.toMessage)
        there was one(context2).writeAndFlush(resetTabEvent.toMessage)
      }
      "broadcast caret events with each other" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")
        client(context1).send(CaretSharingModeRequest("Lily"))

        client(context1).send(moveCaretEvent1, resetCaretEvent1)

        there was one(context2).writeAndFlush(moveCaretEvent1.toMessage)
        there was one(context2).writeAndFlush(resetCaretEvent1.toMessage)
      }
      "broadcast selection events with each other" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")
        client(context1).send(CaretSharingModeRequest("Lily"))

        client(context1).send(selectContentEvent1, resetSelectionEvent)

        there was one(context2).writeAndFlush(selectContentEvent1.toMessage)
        there was one(context2).writeAndFlush(resetSelectionEvent.toMessage)
      }
      "broadcast content events with each other" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")
        client(context1).send(CaretSharingModeRequest("Lily"))

        client(context1).send(changeContentEventA1, resetContentEvent)

        there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
        there was one(context2).writeAndFlush(resetContentEvent.toMessage)
      }
      "send info to impacted users" in new Mocking {
        // need to find a proper way to notify them
        todo
      }
      "can't bind others if it's not in any group" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        client(context2).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
      }
      "can't bind user who is not in the same project" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        client(context1).joinProject("test1")
        client(context2).joinProject("test2")

        client(context1).send(CaretSharingModeRequest("Lily"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Operation is failed because 'Lily' is not in the same project").toMessage)
      }
    }
    "ParallelModeRequest" should {
      "change the mode of client from other mode" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")
        client(context1).send(CaretSharingModeRequest("Lily"), ParallelModeRequest())
        handler.caretSharingModeGroups === Nil
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
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")
        client(context2).send(FollowModeRequest("Freewind"))

        events.foreach { event =>
          client(context1).send(event)
          there was one(context2).writeAndFlush(event.toMessage)
          there was no(context3).writeAndFlush(event.toMessage)
        }
      }
    }
    "FollowModeRequest" should {
      "follow other client" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"))

        handler.followModeMap must havePair("Lily" -> Set("Freewind"))
      }
      "mutli users follow one same user" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"))
        client(context3).send(FollowModeRequest("Lily"))

        handler.followModeMap must havePair("Lily" -> Set("Freewind", "Mike"))
      }
      "not follow self" in new Mocking {
        client(context1).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Freewind"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow self").toMessage)
        handler.followModeMap must beEmpty
      }
      "not follow non-exist user" in new Mocking {
        client(context1).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("non-exist-user"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow non-exist user: 'non-exist-user'").toMessage)
        handler.followModeMap must beEmpty
      }
      "not follow the follower" in new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"))
        client(context2).send(FollowModeRequest("Freewind"))

        there was one(context2).writeAndFlush(ServerErrorResponse("Can't follow your follower: 'Freewind'").toMessage)
        handler.followModeMap must havePair("Lily" -> Set("Freewind"))
      }
      "able to change the target user" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"), FollowModeRequest("Mike"))

        handler.followModeMap === Map("Mike" -> Set("Freewind"))
      }
      "follow the target of a follow" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"))
        client(context3).send(FollowModeRequest("Freewind"))

        handler.followModeMap === Map("Lily" -> Set("Freewind", "Mike"))
      }
      "take all the followers to follow new target" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(FollowModeRequest("Lily"))
        client(context2).send(FollowModeRequest("Mike"))

        handler.followModeMap === Map("Mike" -> Set("Freewind", "Lily"))
      }
      "change the mode of client from other mode" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).send(CaretSharingModeRequest("Lily"), FollowModeRequest("Mike"))
        handler.followModeMap === Map("Mike" -> Set("Freewind"))
        handler.caretSharingModeGroups === Nil
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
        client(context1, context2, context3).active(sendInfo = true).joinProject("test")

        client(context1).follow(context2)

        events.foreach { event =>
          client(context1).send(event)
          there was no(context2).writeAndFlush(event.toMessage)
          there was no(context3).writeAndFlush(event.toMessage)
        }
      }
    }
  }

  "Project request from client" should {
    "CreateProjectRequest" should {
      "not be sent by user who has not sent ClientInfoEvent" in new Mocking {
        client(context1).active(sendInfo = false).send(CreateProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
      }
      "create a new project and join it with a new name on the server" in new Mocking {
        client(context1).active(sendInfo = true).send(CreateProjectRequest("test"))
        handler.projects must haveProjectMembers("test", Set("Freewind"))
      }
      "not create a project with existing name" in new Mocking {
        client(context1).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test"), CreateProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Project 'test' is already exist, can't create again").toMessage)
      }
    }
    "JoinProjectRequest" should {
      "not be sent by user who has not sent ClientInfoEvent" in new Mocking {
        client(context1).active(sendInfo = false).send(JoinProjectRequest("test"))
        there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
      }
      "join an existing project" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test"))
        client(context2).send(JoinProjectRequest("test"))
        handler.projects must haveProjectMembers("test", Set("Freewind", "Lily"))
      }
      "not join an non-exist project" in new Mocking {
        client(context1).active(sendInfo = true).send(JoinProjectRequest("non-exist"))
        there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'non-exist'").toMessage)
      }
      "leave original project when join another" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test1"))
        client(context2).send(CreateProjectRequest("test2"))
        client(context1).send(JoinProjectRequest("test2"))
        handler.projects must haveProjectMembers("test2", Set("Freewind", "Lily"))
      }
    }
    "Project on server" should {
      "be destroyed if no one joined" in new Mocking {
        client(context1).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test1"), CreateProjectRequest("test2"))
        handler.projects must haveSize(1)
        handler.projects must haveProjectMembers("test2", Set("Freewind"))
      }
    }
    "User who has not joined to any project" should {
      "able to receive ServerStatusResponse" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        there was one(context1).writeAndFlush(ServerStatusResponse(
          Nil,
          Seq(ClientInfoData("1.1.1.1", "Freewind", isMaster = false), ClientInfoData("2.2.2.2", "Lily", isMaster = false))
        ).toMessage)
      }
      "able to receive ServerErrorResponse" in new Mocking {
        client(context1, context2).active(sendInfo = true)
        client(context1).send(JoinProjectRequest("any"))
        there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'any'").toMessage)
      }
      "not send editor related events" in new Mocking {
        cannotSendEvents(
          openTabEvent1, closeTabEvent, resetTabEvent,
          changeContentEventA1, resetContentEvent,
          moveCaretEvent1, resetCaretEvent1,
          selectContentEvent1, resetSelectionEvent
        )
      }
      "not send mode related request" in new Mocking {
        cannotSendEvents(
          FollowModeRequest("Lily"),
          CaretSharingModeRequest("Lily"),
          ParallelModeRequest()
        )
      }
      "not send master related request" in new Mocking {
        cannotSendEvents(changeContentEventA1)
      }
      "not send IgnoreFilesRequest related request" in new Mocking {
        cannotSendEvents(IgnoreFilesRequest(Seq("/aaa")))
      }
      "not send SyncFilesRequest related request" in new Mocking {
        cannotSendEvents(syncFilesRequest)
      }
      def cannotSendEvents(events: PairEvent*) = new Mocking {
        client(context1).active(sendInfo = false).send(events: _*)

        there were atLeast(events.size)(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
      }
    }
    "User who has joined to a project" should {
      "only receive events from users in the same project" in new Mocking {
        client(context1, context2, context3).active(sendInfo = true)

        client(context1).send(CreateProjectRequest("p1"))
        client(context2).send(JoinProjectRequest("p1"))
        client(context3).send(CreateProjectRequest("p3"))

        client(context1).send(changeContentEventA1)

        there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
        there was no(context3).writeAndFlush(changeContentEventA1.toMessage)
      }
    }
  }

  def haveProjectMembers(projectName: String, members: Set[String]) =
    beSome[Project].which(_.members == members) ^^ { (x: Map[String, Project]) => x.get(projectName)}

  trait Mocking extends Scope with MockEvents {
    m =>

    private val contexts = new Contexts {}
    private var projects = Map.empty[String, Project]
    private var bindModeGroups = List.empty[Set[String]]
    private var followModeMap = Map.empty[String, Set[String]]
    private var parallelModeClients = Set.empty[String]

    def dataOf(context: ChannelHandlerContext) = {
      handler.contexts.get(context)
    }

    val handler = new ServerHandlerProvider {
      override val contexts = m.contexts
      override def projects = m.projects
      override def projects_=(projects: Map[String, Project]) = m.projects = projects
      override def caretSharingModeGroups = m.bindModeGroups
      override def caretSharingModeGroups_=(groups: List[Set[String]]) = m.bindModeGroups = groups
      override def followModeMap = m.followModeMap
      override def followModeMap_=(map: Map[String, Set[String]]) = m.followModeMap = map
      override def parallelModeClients: Set[String] = m.parallelModeClients
      override def parallelModeClients_=(clients: Set[String]): Unit = m.parallelModeClients = clients
    }

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

    def client(contexts: ChannelHandlerContext*) = new {
      private def singleSend(context: ChannelHandlerContext, event: PairEvent) = {
        handler.channelRead(context, event.toMessage)
      }

      def active(sendInfo: Boolean): this.type = {
        contexts.toList.filterNot(handler.contexts.contains).foreach { ctx =>
          handler.channelActive(ctx)
          if (sendInfo) {
            singleSend(ctx, contextWithInfo(ctx))
          }
        }
        this
      }

      def joinProject(projectName: String): this.type = {
        singleSend(contexts.head, CreateProjectRequest(projectName))
        contexts.tail.foreach(ctx => singleSend(ctx, JoinProjectRequest(projectName)))
        this
      }

      def shareCaret(): this.type = {
        val first = contexts.head
        contexts.tail.foreach(c => singleSend(first, CaretSharingModeRequest(dataOf(c).get.name)))
        this
      }

      def send(events: PairEvent*): this.type = {
        for {
          context <- contexts
          event <- events
        } singleSend(context, event)
        this
      }
      def follow(target: ChannelHandlerContext): this.type = {
        follow(dataOf(target).get.name)
      }
      def follow(name: String): this.type = {
        send(FollowModeRequest(name))
        this
      }
      def beMaster(): this.type = {
        contexts.foreach { context =>
          if (!handler.contexts.contains(context)) {
            handler.contexts.add(context)
          }
          handler.contexts.all.foreach(_.master = false)
          dataOf(context).foreach(_.master = true)
        }
        this
      }
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
