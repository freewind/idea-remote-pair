package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair._
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class WorkingModeSpec extends Specification with Mockito {

  "The default working mode for a new client" should {
    "be CaretSharingMode" in new ProtocolMocking {
      client(context1).active(sendInfo = false)

      there was one(context1).writeAndFlush(
        ClientInfoResponse(project = None, ip = "Unknown", name = "Unknown", isMaster = false).toMessage)
    }
  }

  "CaretSharingMode" should {
    "tell all the clients in caret sharing mode" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test").shareCaret()

      project("test").isSharingCaret === true
    }
    "change the mode of client from other mode" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).parallel().shareCaret()

      project("test").isSharingCaret === true
    }
    "broadcast many events with each other" should {
      def broadcast(events: PairEvent*) = new ProtocolMocking {
        client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

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
      project("test").isSharingCaret === false
    }
  }

}
