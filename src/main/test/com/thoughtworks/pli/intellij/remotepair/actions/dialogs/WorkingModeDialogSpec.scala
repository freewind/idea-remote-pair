package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse
import com.intellij.openapi.project.Project
import scala.Some

class WorkingModeDialogSpec extends Specification with Mockito {

  "Initialization" should {
    "initialize the dialog with clients who is in caret-sharing mode" in new Mocking {
      there was one(form).setClientsInCaretSharingMode(Seq("c1", "c2"))
    }
    "initialize the dialog with clients who can be followed" in new Mocking {
      there was one(form).setClientsInFollowMode(Map("c1" -> Seq("c3")))
    }
  }

  "When 'Next' button clicked, it" should {
    "send CaretSharingModeRequest if user selected caret sharing mode" in new Mocking {
      form.isCaretSharingMode returns true
      dialog.doOKAction()
      await()
      there was one(publishEvent).apply(CaretSharingModeRequest)
    }
    "send FollowModeRequest if user selected one radio in follow mode panel" in new Mocking {
      form.getSelectedClientNameInFollowMode returns Some("aaa")
      dialog.doOKAction()
      await()
      there was one(publishEvent).apply(FollowModeRequest("aaa"))
    }

    "send ParallelModelRequest if user selected the parallel mode radio" in new Mocking {
      form.isParallelMode returns true
      dialog.doOKAction()
      await()
      there was one(publishEvent).apply(ParallelModeRequest)
    }

  }

  "Validation" should {
    "be delegated to inner form" in new Mocking {
      dialog.doValidate()
      there was one(form).validate
    }
  }

  trait Mocking extends Scope {
    self =>
    val project = mock[Project]
    val form = spy(new WorkingModeForm)
    val invokeLater = new MockInvokeLater
    val publishEvent = mock[PairEvent => Unit]
    val showError = mock[String => Any]

    val clientInfoResponse = ClientInfoResponse(Some("test"), "any-ip", "c0", isMaster = false, workingMode = None)
    val serverStatusResponse = createMockServerStatusResponse()

    val dialog = new WorkingModeDialog(project) {
      override def form: WorkingModeForm = self.form
      override def invokeLater(f: => Any): Unit = self.invokeLater(f)
      override def publishEvent(event: PairEvent): Unit = self.publishEvent(event)
      override def showError(message: String): Unit = self.showError(message)
      override def serverStatus: Option[ServerStatusResponse] = Some(serverStatusResponse)
      override def clientInfo: Option[ClientInfoResponse] = Some(clientInfoResponse)
    }
    def await() = invokeLater.await()

    def createMockServerStatusResponse() = {
      val clients = Seq(
        clientInfoResponse,
        new ClientInfoResponse(Some("test"), "any-ip", "c1", true, Some(CaretSharingModeRequest)),
        new ClientInfoResponse(Some("test"), "any-ip", "c2", false, Some(CaretSharingModeRequest)),
        new ClientInfoResponse(Some("test"), "any-ip", "c3", false, Some(FollowModeRequest("c1"))),
        new ClientInfoResponse(Some("test"), "any-ip", "c4", false, Some(ParallelModeRequest))
      )

      val project = ProjectInfoData("test", clients, Nil)

      ServerStatusResponse(Seq(project), Nil)
    }

  }

}
