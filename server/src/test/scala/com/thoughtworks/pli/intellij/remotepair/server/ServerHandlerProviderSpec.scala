package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair.MySpecification
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.protocol.ProtocolMocking

class ServerHandlerProviderSpec extends MySpecification {

  "When client is connected, server" should {
    "ask for client information" in new ProtocolMocking {
      client(context1)
      there was one(context1).writeAndFlush(AskForJoinProject(None).toMessage)
    }
  }

  "ServerHandler" should {
    "add the context to global cache when channelActive" in new ProtocolMocking {
      client(context1)
      handler.clients.size === 1
    }
    "remove the context from global cache when channel is inactive" in new ProtocolMocking {
      client(context1)
      handler.channelInactive(context1)
      handler.clients.size === 0
    }
    "broadcast common received event to other context of same project" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      client(context1).send(changeContentEventA1)

      there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
      there was no(context1).writeAndFlush(changeContentEventA1.toMessage)
    }
  }

}
