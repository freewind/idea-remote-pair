package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, MockCurrentProjectHolder, SendClientNameDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class EventHandlerSpec extends Specification with Mockito with ThrownExpectations {

  "EventHandler" should {
    "handle AskForClientInformation" in new Mocking {
      handler.handleEvent(AskForClientInformation)
      invokeLater.await(1000)
      there was one(dialogCreated).apply("SendClientNameDialog")
    }
    "handle AskForJoinProject" in new Mocking {
      handler.handleEvent(AskForJoinProject)
      invokeLater.await(1000)
      there was one(dialogCreated).apply("JoinProjectDialog")
    }
    "handle AskForWorkingMode" in new Mocking {
      handler.handleEvent(AskForWorkingMode)
      invokeLater.await(1000)
      there was one(dialogCreated).apply("WorkingModeDialog")
    }
  }

  "ClientInfoResponse" should {
    "be kept" in new Mocking {
      handler.handleEvent(clientInfoResponse)
      handler.clientInfo ==== Some(clientInfoResponse)
    }
  }

  trait Mocking extends Scope with MockCurrentProjectHolder {
    m =>

    val invokeLater = new MockInvokeLater

    val dialogCreated = mock[Any => Any]

    class MyEventHandler extends EventHandler with MockCurrentProject {
      override def createSendClientNameDialog() = {
        dialogCreated("SendClientNameDialog")
        mock[SendClientNameDialog]
      }
      override def createJoinProjectDialog() = {
        dialogCreated("JoinProjectDialog")
        mock[JoinProjectDialog]
      }
      override def createWorkingModeDialog() = {
        dialogCreated("WorkingModeDialog")
        mock[WorkingModeDialog]
      }
      override def invokeLater(f: => Any) = m.invokeLater(f)
    }

    val clientInfoResponse = ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true, workingMode = Some(ParallelModeRequest))
    val handler = new MyEventHandler
  }

}
