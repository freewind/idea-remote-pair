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

  "When client is connected, server" should {
    "ask for client information" in new Mocking {
      client(context1).active(sendInfo = false)
      there was one(context1).writeAndFlush(AskForClientInformation().toMessage)
    }
  }

  "ServerHandler" should {
    "add the context to global cache when channelActive" in new Mocking {
      client(context1).active(sendInfo = false)
      handler.contexts.size === 1
    }
    "remove the context from global cache when channel is inactive" in new Mocking {
      client(context1).active(sendInfo = false)
      handler.channelInactive(context1)
      handler.contexts.size === 0
    }
    "can only handle ByteBuf message type" in new Mocking {
      client(context1, context2).active(sendInfo = false)

      handler.channelRead(context1, "unknown-message-type")

      there was no(context2).writeAndFlush(any)
    }
    "broadcast common received event to other context of same project" in new Mocking {
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

      client(context1).send(changeContentEventA1) // will broadcast to other contexts (2,3) as locks
      client(context3).send(changeContentEventA1) // unlock context3
      client(context3).send(changeContentEventA2) // broadcast to (1,2)

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
      project("test").caretSharingModeGroup === Some(Seq("Lily"))
    }
    "be removed from follower groups if it is a star" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).follow(context2)

      handler.channelInactive(context2)
      dataOf(context1).myWorkingMode = None
    }
  }

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1, changeContentEventA2, resetContentEvent)

      dataOf(context2).pathSpecifiedLocks.get("/aaa").map(_.contentLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context2).send(changeContentEventA1)
      client(context1).send(resetContentEvent)

      dataOf(context1).pathSpecifiedLocks.get("/aaa") must beSome.which(_.contentLocks.size === 0)
    }
  }

  "Master context" should {
    "be the first one who joined a project" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      dataOf(context1).master === true
      dataOf(context2).master === false
    }
    "will change to next one automatically if the master is disconnected" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      handler.channelInactive(context1)
      dataOf(context2).master === false
    }
    "changed to the one which is requested" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeMasterEvent)

      dataOf(context1).master === false
      dataOf(context2).master === true
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

      dataOf(context2).projectSpecifiedLocks.activeTabLocks.size === 1
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(openTabEvent1)
      client(context2).send(openTabEvent1)

      dataOf(context2).projectSpecifiedLocks.activeTabLocks.size === 0
      dataOf(context1).projectSpecifiedLocks.activeTabLocks.size === 0
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

      val locks = dataOf(context2).projectSpecifiedLocks.activeTabLocks
      locks.size === 1
      locks.headOption === Some("/ccc")
    }
    "clear the master locks as well" in new Mocking {
      client(context1, context2).active(sendInfo = false)
      client(context1).beMaster()

      client(context2).send(openTabEvent1)
      client(context1).send(resetTabEvent)

      dataOf(context1).projectSpecifiedLocks.activeTabLocks.size === 0
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

      dataOf(context1).name === "Freewind"
      dataOf(context1).ip === "1.1.1.1"
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
    "broadcast to caret-sharing users" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(closeTabEvent)

      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to fans of a star" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context2).follow("Freewind")

      client(context1).send(closeTabEvent)
      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
    "broadcast to following users of a binding mode user" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test").shareCaret()

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

      project("test").ignoredFiles === Some(Seq("/aaa", "/bbb"))
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

  "CaretSharingMode" should {
    "tell all the clients in caret sharing mode" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test").shareCaret()

      project("test").caretSharingModeGroup === Seq("Freewind", "Lily", "Mike")
    }
    "change the mode of client from other mode" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily").shareCaret()

      project("test").caretSharingModeGroup === Seq("Mike", "Freewind")
    }
    "broadcast many events with each other" should {
      def broadcast(events: PairEvent*) = new Mocking {
        client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

        client(context1).send(events: _*)

        events.foreach { event =>
          there was one(context2).writeAndFlush(event.toMessage)
        }
      }
      "include tab events" in new Mocking {
        broadcast(openTabEvent1, closeTabEvent, resetTabEvent)
      }
      "include caret events" in new Mocking {
        broadcast(moveCaretEvent1, resetCaretEvent1)
      }
      "include selection events" in new Mocking {
        broadcast(selectContentEvent1, resetSelectionEvent)
      }
      "include content events" in new Mocking {
        broadcast(changeContentEventA1, resetContentEvent)
      }
    }

    "can't share caret if it's not in any project" in new Mocking {
      client(context1).active(sendInfo = true).shareCaret()

      there was one(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
    }
  }

  "ParallelModeRequest" should {
    "change the mode of client from other mode" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")
      client(context1).shareCaret().parallel()
      project("test").caretSharingModeGroup === Nil
    }
    "only broadcast tab events to followers" in new Mocking {
      sendToFollowersOnly(openTabEvent1, closeTabEvent)
    }
    "only broadcast caret events to followers" in new Mocking {
      sendToFollowersOnly(moveCaretEvent1, resetCaretEvent1)
    }
    "only broadcast selection events to followers" in new Mocking {
      sendToFollowersOnly(selectContentEvent1, resetSelectionEvent)
    }

    def sendToFollowersOnly(events: PairEvent*) = new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")
      client(context3).shareCaret()

      client(context2).follow("Freewind")

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

      client(context1).follow("Lily")

      dataOf(context1).isFollowing(dataOf(context1)) === true
    }
    "not follow self" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).follow("Freewind")
      there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow self").toMessage)
      dataOf(context1).isFollowing(dataOf(context1)) === false
    }
    "not follow non-exist user" in new Mocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).follow("non-exist-user")
      there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow non-exist user: 'non-exist-user'").toMessage)
    }
    "not follow the follower" in new Mocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily")
      client(context2).follow("Freewind")

      there was one(context2).writeAndFlush(ServerErrorResponse("Can't follow your follower: 'Freewind'").toMessage)
      dataOf(context1).isFollowing(dataOf(context1)) === false
    }
    "able to change the star" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily").follow("Mike")

      dataOf(context1).isFollowing(dataOf(context3)) === true
    }
    "not follow a fan" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily")
      client(context3).follow("Freewind")

      there was one(context3).writeAndFlush(ServerErrorResponse("Can't follow a follower: 'Freewind'").toMessage)
    }
    "change the mode of client from other mode" in new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).shareCaret().follow("Mike")
      dataOf(context1).isFollowing(dataOf(context3)) === true
      project("test").caretSharingModeGroup === Nil
    }
    "not broadcast content events to others" in new Mocking {
      willNotBroadcastToOthers(changeContentEventA1, resetContentEvent)
    }
    "not broadcast tab events to others" in new Mocking {
      willNotBroadcastToOthers(openTabEvent1, closeTabEvent, resetTabEvent)
    }
    "not broadcast caret events to others" in new Mocking {
      willNotBroadcastToOthers(moveCaretEvent1, resetCaretEvent1)
    }
    "not broadcast selection events to others" in new Mocking {
      willNotBroadcastToOthers(selectContentEvent1, resetSelectionEvent)
    }
    "not broadcast file events to others" in new Mocking {
      willNotBroadcastToOthers(createFileEvent, deleteFileEvent, createDirEvent, deleteDirEvent, renameEvent)
    }

    def willNotBroadcastToOthers(events: PairEvent*) = new Mocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow(context2)

      events.foreach { event =>
        client(context1).send(event)
        there was no(context2).writeAndFlush(event.toMessage)
        there was no(context3).writeAndFlush(event.toMessage)
      }
    }
  }

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

    "Project on server" should {
      "be kept even if no one joined" in new Mocking {
        client(context1).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test1"), CreateProjectRequest("test2"))
        handler.projects.all.size === 2
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
          CaretSharingModeRequest,
          ParallelModeRequest
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
    beSome[Project].which(_.members == members) ^^ { (x: Projects) => x.get(projectName)}

  trait Mocking extends Scope with MockEvents {
    m =>

    private val contexts = new Contexts {}
    val projects = new Projects {}
    def dataOf(context: ChannelHandlerContext) = {
      handler.contexts.get(context).get
    }

    val handler = new ServerHandlerProvider {
      override val contexts = m.contexts
      override val projects = m.projects
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
        send(CaretSharingModeRequest)
        this
      }

      def parallel(): this.type = {
        send(ParallelModeRequest)
      }

      def send(events: PairEvent*): this.type = {
        for {
          context <- contexts
          event <- events
        } singleSend(context, event)
        this
      }
      def follow(target: ChannelHandlerContext): this.type = {
        follow(dataOf(target).name)
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
          dataOf(context).master = true
        }
        this
      }
    }

    def caretLock(context: ChannelHandlerContext, path: String) = {
      dataOf(context).pathSpecifiedLocks.get(path).map(_.caretLocks)
    }

    def selectionLock(context: ChannelHandlerContext, path: String) = {
      dataOf(context).pathSpecifiedLocks.get(path).map(_.selectionLocks)
    }

    def project(name: String) = projects.get(name).get
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
