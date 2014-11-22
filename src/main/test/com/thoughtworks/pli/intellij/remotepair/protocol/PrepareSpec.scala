package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.AskForClientInformation
import com.thoughtworks.pli.intellij.remotepair.ServerErrorResponse
import com.thoughtworks.pli.intellij.remotepair.ClientInfoEvent
import com.thoughtworks.pli.intellij.remotepair.AskForJoinProject
import com.thoughtworks.pli.intellij.remotepair.server.{Projects, Project}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class PrepareSpec extends Specification with Mockito {

  "ClientInfoEvent" should {
    "store client name and ip to context data" in new ProtocolMocking {
      client(context1).active(sendInfo = true)

      dataOf(context1).name === "Freewind"
      dataOf(context1).ip === "1.1.1.1"
    }
    "get an error back if the name is blank, and ask for information again" in new ProtocolMocking {
      client(context1).active(sendInfo = false)
      org.mockito.Mockito.reset(context1)

      client(context1).send(ClientInfoEvent("non-empty-ip", "  "))
      there was one(context1).writeAndFlush(ServerErrorResponse("Name is not provided").toMessage)
      there was one(context1).writeAndFlush(AskForClientInformation.toMessage)
    }
    "get an error back if the name is already existing, and ask for information again" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = false)
      org.mockito.Mockito.reset(context2)

      client(context1).send(ClientInfoEvent("non-empty-ip", "Freewind"))
      client(context2).send(ClientInfoEvent("non-empty-ip", "Freewind"))
      there was one(context2).writeAndFlush(ServerErrorResponse("Specified name 'Freewind' is already existing").toMessage)
      there was one(context2).writeAndFlush(AskForClientInformation.toMessage)
    }
  }

  "AskForJoinProject" should {
    "send to client if has gotten client's information" in new ProtocolMocking {
      client(context1).active(sendInfo = true)
      there was one(context1).writeAndFlush(AskForJoinProject.toMessage)
    }
  }

  "CreateProjectRequest" should {
    "not be sent by user who has not sent ClientInfoEvent" in new ProtocolMocking {
      client(context1).active(sendInfo = false).send(CreateProjectRequest("test"))
      there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
    }
    "create a new project and join it with a new name on the server" in new ProtocolMocking {
      client(context1).active(sendInfo = true).send(CreateProjectRequest("test"))
      handler.projects must haveProjectMembers("test", Seq("Freewind"))
    }
    "not create a project with existing name" in new ProtocolMocking {
      client(context1).active(sendInfo = true)
      client(context1).send(CreateProjectRequest("test"), CreateProjectRequest("test"))
      there was one(context1).writeAndFlush(ServerErrorResponse("Project 'test' is already exist, can't create again").toMessage)
    }
  }

  "JoinProjectRequest" should {
    "not be sent by user who has not sent ClientInfoEvent" in new ProtocolMocking {
      client(context1).active(sendInfo = false).send(JoinProjectRequest("test"))
      there was one(context1).writeAndFlush(ServerErrorResponse("Please tell me your information first").toMessage)
    }
    "join an existing project" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true)
      client(context1).send(CreateProjectRequest("test"))
      client(context2).send(JoinProjectRequest("test"))
      handler.projects must haveProjectMembers("test", Seq("Freewind", "Lily"))
    }
    "not join an non-exist project" in new ProtocolMocking {
      client(context1).active(sendInfo = true).send(JoinProjectRequest("non-exist"))
      there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'non-exist'").toMessage)
    }
    "leave original project when join another" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true)
      client(context1).send(CreateProjectRequest("test1"))
      client(context2).send(CreateProjectRequest("test2"))
      client(context1).send(JoinProjectRequest("test2"))
      handler.projects must haveProjectMembers("test2", Seq("Lily", "Freewind"))
    }

    "Project on server" should {
      "be kept even if no one joined" in new ProtocolMocking {
        client(context1).active(sendInfo = true)
        client(context1).send(CreateProjectRequest("test1"), CreateProjectRequest("test2"))
        handler.projects.all.size === 2
        handler.projects must haveProjectMembers("test2", Seq("Freewind"))
      }
    }
    "User who has not joined to any project" should {
      "able to receive ServerStatusResponse" in new ProtocolMocking {
        client(context1, context2).active(sendInfo = true)
        there was one(context1).writeAndFlush(ServerStatusResponse(
          Nil,
          Seq(ClientInfoResponse(project = None, ip = "1.1.1.1", name = "Freewind", isMaster = false, workingMode = Some(CaretSharingModeRequest)),
            ClientInfoResponse(project = None, "2.2.2.2", "Lily", isMaster = false, workingMode = Some(CaretSharingModeRequest)))
        ).toMessage)
      }
      "able to receive ServerErrorResponse" in new ProtocolMocking {
        client(context1, context2).active(sendInfo = true)
        client(context1).send(JoinProjectRequest("any"))
        there was one(context1).writeAndFlush(ServerErrorResponse("You can't join a non-existent project: 'any'").toMessage)
      }
      "not send editor related events" in new ProtocolMocking {
        cannotSendEvents(
          openTabEvent1, closeTabEvent, resetTabEvent,
          changeContentEventA1, resetContentEvent,
          moveCaretEvent1, resetCaretEvent1,
          selectContentEvent1, resetSelectionEvent
        )
      }
      "not send mode related request" in new ProtocolMocking {
        cannotSendEvents(
          FollowModeRequest("Lily"),
          CaretSharingModeRequest,
          ParallelModeRequest
        )
      }
      "not send master related request" in new ProtocolMocking {
        cannotSendEvents(changeContentEventA1)
      }
      "not send IgnoreFilesRequest related request" in new ProtocolMocking {
        cannotSendEvents(IgnoreFilesRequest(Seq("/aaa")))
      }
      "not send SyncFilesRequest related request" in new ProtocolMocking {
        cannotSendEvents(syncFilesRequest)
      }
      def cannotSendEvents(events: PairEvent*) = new ProtocolMocking {
        client(context1).active(sendInfo = false).send(events: _*)

        there were atLeast(events.size)(context1).writeAndFlush(ServerErrorResponse("Operation is not allowed because you have not joined in any project").toMessage)
      }
    }
    "User who has joined to a project" should {
      "only receive events from users in the same project" in new ProtocolMocking {
        client(context1, context2, context3).active(sendInfo = true)

        client(context1).send(CreateProjectRequest("p1"))
        client(context2).send(JoinProjectRequest("p1"))
        client(context3).send(CreateProjectRequest("p3"))

        client(context1).send(changeContentEventA1)

        there was one(context2).writeAndFlush(changeContentEventA1.toMessage)
        there was no(context3).writeAndFlush(changeContentEventA1.toMessage)
      }
    }
  }

  "ClientInfoResponse" should {
    "be sent to client when client info changes" in new ProtocolMocking {
      client(context1).active(sendInfo = true)
      there was one(context1).writeAndFlush(ClientInfoResponse(project = None, ip = "1.1.1.1", name = "Freewind", isMaster = false, workingMode = Some(CaretSharingModeRequest)).toMessage)
    }
    "be sent to client when join a project" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test1")
      there was one(context1).writeAndFlush(ClientInfoResponse(Some("test1"), "1.1.1.1", "Freewind", isMaster = true, workingMode = Some(CaretSharingModeRequest)).toMessage)
    }
    "be sent to client when working mode changes" in new ProtocolMocking {
      client(context1).active(sendInfo = true).joinProject("test1")
      resetMock(context1)
      client(context1).shareCaret()
      there was one(context1).writeAndFlush(ClientInfoResponse(Some("test1"), "1.1.1.1", "Freewind", isMaster = true, workingMode = Some(CaretSharingModeRequest)).toMessage)
    }
  }

  def haveProjectMembers(projectName: String, members: Seq[String]) = {
    beSome[Project].which(_.members.map(_.name) === members) ^^ { (x: Projects) => x.get(projectName)}
  }


}
