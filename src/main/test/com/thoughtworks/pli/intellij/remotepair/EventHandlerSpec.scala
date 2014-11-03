package com.thoughtworks.pli.intellij.remotepair

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{WorkingModeDialog, JoinProjectDialog, SendClientNameDialog}

class EventHandlerSpec extends Specification with Mockito {

  "EventHandler" should {
    "handle AskForClientInformation" in new Mocking {
      handler.handleEvent(AskForClientInformation())
      there was one(handler).createSendClientNameDialog()
    }
    "handle AskForJoinProject" in new Mocking {
      handler.handleEvent(AskForJoinProject())
      there was one(handler).createJoinProjectDialog()
    }
    "handle AskForWorkingMode" in new Mocking {
      handler.handleEvent(AskForWorkingMode())
      there was one(handler).createWorkingModeDialog()
    }
  }

  trait Mocking extends Scope {
    val project = mock[Project]

    class MyEventHandler extends EventHandler with CurrentProjectHolder {
      override def currentProject = project
      override def createSendClientNameDialog() = mock[SendClientNameDialog]
      override def createJoinProjectDialog() = mock[JoinProjectDialog]
      override def createWorkingModeDialog(): WorkingModeDialog = mock[WorkingModeDialog]
    }

    val handler = spy(new MyEventHandler)
  }

}
