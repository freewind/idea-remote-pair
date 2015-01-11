package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, MockCurrentProjectHolder, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import com.thoughtworks.pli.intellij.remotepair.protocol._

class EventHandlerSpec extends MySpecification {

  "EventHandler" should {
    "handle AskForJoinProject" in new Mocking {
      handler.handleEvent(AskForJoinProject(None))
      invokeLater.await(1000)
      there was one(dialogCreated).apply("JoinProjectDialog")
    }
  }

  "ClientInfoResponse" should {
    "be kept" in new Mocking {
      handler.handleEvent(clientInfoResponse)
      there was one(handler.currentProject).clientInfo_=(Some(clientInfoResponse))
    }
  }

  trait Mocking extends MyMocking with MockCurrentProjectHolder {
    m =>

    val invokeLater = new MockInvokeLater

    val dialogCreated = mock[Any => Any]

    val publishEvent = mock[PairEvent => Any]

    class MyEventHandler extends EventHandler with MockCurrentProject {
      override def createJoinProjectDialog(message: Option[String]) = {
        dialogCreated("JoinProjectDialog")
        mock[JoinProjectDialog]
      }
      override def createWorkingModeDialog() = {
        dialogCreated("WorkingModeDialog")
        mock[WorkingModeDialog]
      }
      override def invokeLater(f: => Any) = m.invokeLater(f)
      override def publishEvent(event: PairEvent) = m.publishEvent(event)
      override def runReadAction(f: => Any) = f
      override def runWriteAction(f: => Any) = f
      override def md5(s: String): String = s + "-md5"
    }

    val clientInfoResponse = ClientInfoResponse("cid", "test", "Freewind", isMaster = true)

    val textEditor = deepMock[Editor]

    def mockEditorContent(path: String, content: String) {
      textEditor.getDocument.getText returns content
      currentProject.getTextEditorsOfPath(path) returns Seq(textEditor)
    }

    val handler = new MyEventHandler
  }

}
