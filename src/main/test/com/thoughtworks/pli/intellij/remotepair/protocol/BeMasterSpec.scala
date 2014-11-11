package com.thoughtworks.pli.intellij.remotepair.protocol

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.ServerErrorResponse

class BeMasterSpec extends Specification with Mockito {

  "Master context" should {
    "be the first one who joined a project" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      dataOf(context1).master === true
      dataOf(context2).master === false
    }
    "will change to next one automatically if the master is disconnected" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      handler.channelInactive(context1)
      dataOf(context2).master === false
    }
    "changed to the one which is requested" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeMasterEvent)

      dataOf(context1).master === false
      dataOf(context2).master === true
    }
    "response error message if specified name is not exist" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").send(changeMasterEvent)

      there was one(context1).writeAndFlush(ServerErrorResponse(s"Specified user 'Lily' is not found").toMessage)
    }
  }


}
