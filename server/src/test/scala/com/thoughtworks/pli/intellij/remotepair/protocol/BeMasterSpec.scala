package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.{MySpecification, ServerErrorResponse}

class BeMasterSpec extends MySpecification {

  "Master context" should {
    "be the first one who joined a project" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      dataOf(context1).isMaster === true
      dataOf(context2).isMaster === false
    }
    "will change to next one automatically if the master is disconnected" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      handler.channelInactive(context1)
      dataOf(context2).isMaster === true
    }
    "changed to the one which is requested" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      client(context1).send(changeMasterEvent)

      dataOf(context1).isMaster === false
      dataOf(context2).isMaster === true
    }
    "response error message if specified name is not exist" in new ProtocolMocking {
      client(context1).createOrJoinProject("test").send(changeMasterEvent)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }


}
