package com.thoughtworks.pli.intellij.remotepair

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.SendClientNameDialog

class EventHandlerSpec extends Specification with Mockito {

  "EventHandler" should {
    "handle AskForClientInformation" in new Mocking {
      handler.handleEvent(AskForClientInformation())
      there was one(handler).createSendClientNameDialog()
    }
  }

  trait Mocking extends Scope {
    val project = mock[Project]

    class MyEventHandler extends EventHandler with CurrentProjectHolder {
      override def currentProject = project
      override def createSendClientNameDialog() = mock[SendClientNameDialog]
    }

    val handler = spy(new MyEventHandler)
  }

}
