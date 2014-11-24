package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair._
import scala.Some
import org.mockito.Mockito.{reset => mockReset}
import com.thoughtworks.pli.intellij.remotepair.protocol.ProtocolMocking

class ServerHandlerProviderSpec extends Specification with Mockito {

  "When client is connected, server" should {
    "ask for client information" in new ProtocolMocking {
      client(context1).active(sendInfo = false)
      there was one(context1).writeAndFlush(AskForClientInformation.toMessage)
    }
  }

  "ServerHandler" should {
    "add the context to global cache when channelActive" in new ProtocolMocking {
      client(context1).active(sendInfo = false)
      handler.contexts.size === 1
    }
    "remove the context from global cache when channel is inactive" in new ProtocolMocking {
      client(context1).active(sendInfo = false)
      handler.channelInactive(context1)
      handler.contexts.size === 0
    }
    "broadcast common received event to other context of same project" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1)

      there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
      there was no(context1).writeAndFlush(changeContentEventA1.toMessage)
    }
  }

  "Content event locks" should {
    "be added from sent ChangeContentEvent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1)

      handler.contexts.get(context2) must beSome.which(_.pathSpecifiedLocks.size === 1)
    }
    "be added from ChangeContentEvent from different sources" in new ProtocolMocking {
      client(context1, context2, context3).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1) // will broadcast to other contexts (2,3) as locks
      client(context3).send(changeContentEventA1) // unlock context3
      client(context3).send(changeContentEventA2) // broadcast to (1,2)

      handler.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.size === 1
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(2)
      }
    }
    "clear the first lock if a feedback event matched and it won't be broadcasted" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1) // will broadcast to context2 as lock
      client(context2).send(changeContentEventA1SameSummary)

      handler.contexts.get(context2) must beSome.which { data =>
        data.pathSpecifiedLocks.get("/aaa").map(_.contentLocks.size) === Some(0)
      }
      there was no(context1).writeAndFlush(changeContentEventA1SameSummary.toMessage)
    }
    "send a ResetContentRequest if the feedback event is not matched for the same file path" in new ProtocolMocking {
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

}
