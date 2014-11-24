package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.{ChangeMasterEvent, ClientInfoResponse, ProjectInfoData, ServerStatusResponse, _}

class ServerStatusSpec extends MySpecification {
  "ServerStatusResponse" should {
    "be sent automatically when there is new client joined a project" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client updated info" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client changed to caret sharing mode" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").shareCaret()
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client changed to parallel mode" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").parallel()
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when master changed" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).send(ChangeMasterEvent("Lily"))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test",
          Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = false),
            ClientInfoResponse(Some("test"), "2.2.2.2", "Lily", isMaster = true)),
          Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client disconnected" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      org.mockito.Mockito.reset(context1)

      handler.channelInactive(context2)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when ignored files changed" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).send(IgnoreFilesRequest(Seq("/aaa")))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Seq("/aaa"))),
        Nil
      ).toMessage)
    }
    "contain free clients" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true)
      client(context1).joinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true)), Nil)),
        Seq(ClientInfoResponse(project = None, ip = "2.2.2.2", name = "Lily", isMaster = false))
      ).toMessage)
    }
  }

  "IgnoreFilesRequest" should {
    "store the files on server" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").send(IgnoreFilesRequest(Seq("/aaa", "/bbb")))

      project("test").ignoredFiles === Seq("/aaa", "/bbb")
    }
  }

}
