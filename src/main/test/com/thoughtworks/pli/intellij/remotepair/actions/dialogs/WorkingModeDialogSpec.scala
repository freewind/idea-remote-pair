package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.MySpecification
import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse
import com.intellij.openapi.project.Project

class WorkingModeDialogSpec extends MySpecification {

  "When 'Next' button clicked, it" should {
    "send CaretSharingModeRequest if user selected caret sharing mode" in new Mocking {
      form.isCaretSharingMode returns true
      invokeLater(dialog.doOKAction()).await()
      there was one(publishEvent).apply(CaretSharingModeRequest)
    }
    "send ParallelModelRequest if user selected the parallel mode radio" in new Mocking {
      form.isParallelMode returns true
      invokeLater(dialog.doOKAction()).await()
      there was one(publishEvent).apply(ParallelModeRequest)
    }
  }

  "Validation" should {
    "be delegated to inner form" in new Mocking {
      invokeLater(dialog.doValidate()).await()
      there was one(form).validate
    }
  }

  trait Mocking extends Scope {
    self =>
    val raw = mock[Project]
    val project = mock[RichProject]
    project.raw returns raw
    project.serverStatus returns Some(serverStatusResponse)
    project.clientInfo returns Some(clientInfoResponse)

    val form = spy(new WorkingModeForm)
    val invokeLater = new MockInvokeLater
    val publishEvent = mock[PairEvent => Unit]
    val showError = mock[String => Any]

    val clientInfoResponse = ClientInfoResponse(Some("test"), "any-ip", "c0", isMaster = false)
    val serverStatusResponse = createMockServerStatusResponse()

    lazy val dialog = new WorkingModeDialog(project) {
      override def form: WorkingModeForm = self.form
      override def invokeLater(f: => Any): Unit = self.invokeLater(f)
      override def publishEvent(event: PairEvent): Unit = self.publishEvent(event)
      override def showError(message: String): Unit = self.showError(message)
    }

    def createMockServerStatusResponse() = {
      val clients = Seq(
        clientInfoResponse,
        new ClientInfoResponse(Some("test"), "any-ip", "c1", true),
        new ClientInfoResponse(Some("test"), "any-ip", "c2", false),
        new ClientInfoResponse(Some("test"), "any-ip", "c3", false),
        new ClientInfoResponse(Some("test"), "any-ip", "c4", false)
      )

      val project = ProjectInfoData("test", clients, Nil, WorkingMode.CaretSharing)

      ServerStatusResponse(Seq(project), Nil)
    }

  }

}
