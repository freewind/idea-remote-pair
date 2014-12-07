package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.MySpecification
import com.thoughtworks.pli.intellij.remotepair._

class WorkingModeSpec extends MySpecification {

  "CaretSharingMode" should {
    "tell all the clients in caret sharing mode" in new ProtocolMocking {
      client(context1, context2, context3).createOrJoinProject("test").shareCaret()

      project("test").isSharingCaret === true
    }
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      client(context1).parallel().shareCaret()

      project("test").isSharingCaret === true
    }
    "broadcast many events with each other" should {
      def broadcast(events: PairEvent*) = new ProtocolMocking {
        client(context1, context2).createOrJoinProject("test").shareCaret()

        client(context1).send(events: _*)

        events.foreach { event =>
          there was one(context2).writeAndFlush(event.toMessage)
          there was no(context1).writeAndFlush(event.toMessage)
        }
      }
      "include tab events" in new ProtocolMocking {
        broadcast(openTabEvent1, closeTabEvent, resetTabEvent)
      }
      "include caret events" in new ProtocolMocking {
        broadcast(moveCaretEvent1)
      }
      "include selection events" in new ProtocolMocking {
        broadcast(selectContentEvent1)
      }
      "include content events" in new ProtocolMocking {
        broadcast(changeContentEventA1, resetContentEvent)
      }
    }

    "can't share caret if it's not in any project" in new ProtocolMocking {
      client(context1).shareCaret()

      there was one(context1).writeAndFlush(AskForJoinProject(Some("You need to join a project first")).toMessage)
    }
  }

  "ParallelModeRequest" should {
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1).createOrJoinProject("test")
      client(context1).shareCaret().parallel()
      project("test").isSharingCaret === false
    }
  }

}
