package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.{ServerErrorResponse, PairEvent}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class WorkingModeSpec extends Specification with Mockito {

  "CaretSharingMode" should {
    "tell all the clients in caret sharing mode" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test").shareCaret()

      project("test").caretSharingModeGroup === Seq(dataOf(context1), dataOf(context2), dataOf(context3))
    }
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily").shareCaret()

      project("test").caretSharingModeGroup === Seq(dataOf(context1))
    }
    "broadcast many events with each other" should {
      def broadcast(events: PairEvent*) = new ProtocolMocking {
        client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

        client(context1).send(events: _*)

        events.foreach { event =>
          there was one(context2).writeAndFlush(event.toMessage)
        }
      }
      "include tab events" in new ProtocolMocking {
        broadcast(openTabEvent1, closeTabEvent, resetTabEvent)
      }
      "include caret events" in new ProtocolMocking {
        broadcast(moveCaretEvent1, resetCaretEvent1)
      }
      "include selection events" in new ProtocolMocking {
        broadcast(selectContentEvent1, resetSelectionEvent)
      }
      "include content events" in new ProtocolMocking {
        broadcast(changeContentEventA1, resetContentEvent)
      }
    }

    "can't share caret if it's not in any project" in new ProtocolMocking {
      client(context1).active(sendInfo = true).shareCaret()

      there was one(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
    }
  }

  "ParallelModeRequest" should {
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")
      client(context1).shareCaret().parallel()
      project("test").caretSharingModeGroup === Nil
    }
    "only broadcast tab events to followers" in new ProtocolMocking {
      sendToFollowersOnly(openTabEvent1, closeTabEvent)
    }
    "only broadcast caret events to followers" in new ProtocolMocking {
      sendToFollowersOnly(moveCaretEvent1, resetCaretEvent1)
    }
    "only broadcast selection events to followers" in new ProtocolMocking {
      sendToFollowersOnly(selectContentEvent1, resetSelectionEvent)
    }

    def sendToFollowersOnly(events: PairEvent*) = new ProtocolMocking {
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
    "follow other client" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily")

      dataOf(context1).isFollowing(dataOf(context2)) === true
    }
    "not follow self" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).follow("Freewind")
      there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow self").toMessage)
      dataOf(context1).isFollowing(dataOf(context1)) === false
    }
    "not follow non-exist user" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).follow("non-exist-user")
      there was one(context1).writeAndFlush(ServerErrorResponse("Can't follow non-exist user: 'non-exist-user'").toMessage)
    }
    "not follow a follower" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily")
      client(context3).follow("Freewind")

      there was one(context3).writeAndFlush(ServerErrorResponse("Can't follow a follower: 'Freewind'").toMessage)
      dataOf(context1).isFollowing(dataOf(context1)) === false
    }
    "able to change the star" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily").follow("Mike")

      dataOf(context1).isFollowing(dataOf(context3)) === true
    }
    "not follow a fan" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).follow("Lily")
      client(context3).follow("Freewind")

      there was one(context3).writeAndFlush(ServerErrorResponse("Can't follow a follower: 'Freewind'").toMessage)
    }
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).shareCaret().follow("Mike")
      dataOf(context1).isFollowing(dataOf(context3)) === true
      project("test").caretSharingModeGroup === Nil
    }
    "not broadcast content events to others" in new ProtocolMocking {
      willNotBroadcastToOthers(changeContentEventA1, resetContentEvent)
    }
    "not broadcast tab events to others" in new ProtocolMocking {
      willNotBroadcastToOthers(openTabEvent1, closeTabEvent, resetTabEvent)
    }
    "not broadcast caret events to others" in new ProtocolMocking {
      willNotBroadcastToOthers(moveCaretEvent1, resetCaretEvent1)
    }
    "not broadcast selection events to others" in new ProtocolMocking {
      willNotBroadcastToOthers(selectContentEvent1, resetSelectionEvent)
    }
    "not broadcast file events to others" in new ProtocolMocking {
      willNotBroadcastToOthers(createFileEvent, deleteFileEvent, createDirEvent, deleteDirEvent, renameEvent)
    }

    def willNotBroadcastToOthers(events: PairEvent*) = new ProtocolMocking {
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
