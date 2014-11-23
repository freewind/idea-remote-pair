package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor.TextEditor
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, MockCurrentProjectHolder, SendClientNameDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import com.thoughtworks.pli.intellij.{MyMocking, MySpecification}

class EventHandlerSpec extends MySpecification {

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

  "When received ResetContentRequest, it" should {
    "publish a corresponding ResetContentEvent to server" in new Mocking {
      mockEditorContent("/aaa", "test-content")
      handler.handleEvent(ResetContentRequest("/aaa"))
      there was one(publishEvent).apply(ResetContentEvent("/aaa", "test-content", "test-content-md5"))
    }
  }

  "ChangeContentEvent" should {
    "publish a ResetContentRequet if error occurs when apply it" in new Mocking {
      mockEditorContent("/aaa", "short-content")
      textEditor.getEditor.getDocument.replaceString(any, any, any) throws new RuntimeException("test-edit-error")
      handler.handleEvent(ChangeContentEvent("/aaa", 1000, "aaa", "bbb", "summary"))
      there was one(publishEvent).apply(ResetContentRequest("/aaa"))
    }
  }

  trait Mocking extends MyMocking with MockCurrentProjectHolder {
    m =>

    val invokeLater = new MockInvokeLater

    val dialogCreated = mock[Any => Any]

    val publishEvent = mock[PairEvent => Any]

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
      override def publishEvent(event: PairEvent) = m.publishEvent(event)
      override def runReadAction(f: => Any) = f
      override def runWriteAction(f: => Any) = f
      override def md5(s: String): String = s + "-md5"
    }

    val clientInfoResponse = ClientInfoResponse(Some("test"), "1.1.1.1", "Freewind", isMaster = true, workingMode = Some(ParallelModeRequest))

    val textEditor = deepMock[TextEditor]

    def mockEditorContent(path: String, content: String) {
      textEditor.getEditor.getDocument.getText returns content
      currentProject.getTextEditorsOfPath(path) returns Seq(textEditor)
    }

    val handler = new MyEventHandler
  }

}
