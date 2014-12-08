package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.MySpecification
import com.thoughtworks.pli.intellij.remotepair.{ChangeMasterEvent, ClientInfoResponse, ProjectInfoData, ServerStatusResponse, _}

class ServerStatusSpec extends MySpecification {
  "ServerStatusResponse" should {
    "be sent automatically when there is new client joined a project" in new ProtocolMocking {
      client(context1).createOrJoinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when client updated info" in new ProtocolMocking {
      client(context1).createOrJoinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when client changed to caret sharing mode" in new ProtocolMocking {
      client(context1).createOrJoinProject("test").shareCaret()
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when client changed to parallel mode" in new ProtocolMocking {
      client(context1).createOrJoinProject("test").parallel()
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when master changed" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")
      client(context1).send(ChangeMasterEvent("Lily"))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test",
          Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = false),
            ClientInfoResponse(clientId(context2), "test", "Lily", isMaster = true)),
          Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when client disconnected" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test")

      org.mockito.Mockito.reset(context1)

      handler.channelInactive(context2)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "be sent automatically when ignored files changed" in new ProtocolMocking {
      client(context1).createOrJoinProject("test")

      client(context1).send(IgnoreFilesRequest(Seq("/aaa")))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Seq("/aaa"), WorkingMode.CaretSharing)),
        freeClients = 0
      ).toMessage)
    }
    "contain free clients" in new ProtocolMocking {
      client(context1, context2)
      client(context1).createOrJoinProject("test")
      there was atLeastOne(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse(clientId(context1), "test", "Freewind", isMaster = true)), Nil, WorkingMode.CaretSharing)),
        freeClients = 1
      ).toMessage)
    }
  }

  "IgnoreFilesRequest" should {
    "store the files on server" in new ProtocolMocking {
      client(context1).createOrJoinProject("test").send(IgnoreFilesRequest(Seq("/aaa", "/bbb")))

      project("test").ignoredFiles === Seq("/aaa", "/bbb")
    }
  }

}
