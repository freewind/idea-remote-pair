package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.ChangeMasterEvent
import com.thoughtworks.pli.intellij.remotepair.ClientInfoResponse
import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse
import com.thoughtworks.pli.intellij.remotepair.ProjectInfoData
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class ServerStatusSpec extends Specification with Mockito {
  "ServerStatusResponse" should {
    "be sent automatically when there is new client joined a project" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = None)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client updated info" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = None)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client changed to caret sharing mode" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").shareCaret()
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = Some(CaretSharingModeRequest))), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client changed to follow mode" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).follow(context2)

      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(
          ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = Some(FollowModeRequest("Lily"))),
          ClientInfoResponse("2.2.2.2", "Lily", isMaster = false, workingMode = None)
        ), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client changed to parallel mode" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test").parallel()
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = Some(ParallelModeRequest))), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when master changed" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")
      client(context1).send(ChangeMasterEvent("Lily"))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test",
          Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = false, workingMode = None),
            ClientInfoResponse("2.2.2.2", "Lily", isMaster = true, workingMode = None)),
          Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when client disconnected" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      org.mockito.Mockito.reset(context1)

      handler.channelInactive(context2)
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = None)), Nil)),
        Nil
      ).toMessage)
    }
    "be sent automatically when ignored files changed" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test")

      client(context1).send(IgnoreFilesRequest(Seq("/aaa")))
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = None)), Seq("/aaa"))),
        Nil
      ).toMessage)
    }
    "contain free clients" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true)
      client(context1).joinProject("test")
      there was one(context1).writeAndFlush(ServerStatusResponse(
        Seq(ProjectInfoData("test", Seq(ClientInfoResponse("1.1.1.1", "Freewind", isMaster = true, workingMode = None)), Nil)),
        Seq(ClientInfoResponse("2.2.2.2", "Lily", isMaster = false, workingMode = None))
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
