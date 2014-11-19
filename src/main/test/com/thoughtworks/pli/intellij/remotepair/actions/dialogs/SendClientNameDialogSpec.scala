package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.actions.forms.SendClientNameForm
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{PairEvent, ClientInfoEvent}
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater

class SendClientNameDialogSpec extends Specification with Mockito {

  "SendClientNameDialog" should {
    "use 'client name' from application store to init the text field" in new Mocking {
      invokeLater(dialog).await()
      there was one(form).clientName_=("test-client-name")
    }
  }

  "Validation" should {
    "involve form' validation" in new Mocking {
      invokeLater(dialog.doValidate()).await()

      there was one(form).validate
    }
  }

  "'Next' button" should {
    "be enabled at first" in new Mocking {
      invokeLater(dialog.isOKActionEnabled === true).await()
    }
  }

  "Clicking on 'Next' button" should {
    "save the 'client name' to application store" in new Mocking {
      invokeLater(dialog.doOKAction()).await()
      there was one(dialog.appProperties).clientName_=("test-client-name")
    }
    "send the 'client name' with 'local ip' to server" in new Mocking {
      invokeLater(dialog.doOKAction()).await()
      there was one(mockPublishEvent).apply(ClientInfoEvent("test-local-ip", "test-client-name"))
    }
    "show error if there is some error(e.g. Network issue)" in new Mocking {
      mockPublishEvent.apply(any) throws new RuntimeException("test-error")
      invokeLater(dialog.doOKAction()).await()
      there was one(mockShowError).apply(any)
    }

    "close the dialog if sending successfully" in new Mocking {
      invokeLater(dialog.doOKAction()).await()
      dialog.getExitCode === 0
    }
  }

  trait Mocking extends Scope {
    self =>

    val form = spy(new SendClientNameForm)
    val project = mock[Project]
    val mockPublishEvent = mock[PairEvent => Unit]
    val mockShowError = mock[String => Unit]
    val invokeLater = new MockInvokeLater

    class MockDialog extends SendClientNameDialog(project) {

      object EarlyInit {
        val mockProjectProperties = mock[AppProperties]
        mockProperties(mockProjectProperties)
      }

      def mockProperties(appProps: AppProperties) {
        appProps.clientName returns "test-client-name"
      }

      override def form: SendClientNameForm = self.form
      override def appProperties: AppProperties = EarlyInit.mockProjectProperties
      override def localIp(): String = "test-local-ip"
      override def invokeLater(f: => Any) = self.invokeLater(f)
      override def publishEvent(event: PairEvent) = {
        mockPublishEvent.apply(event)
      }
      override def showError(message: String) = mockShowError.apply(message)
      override def doOKAction() = invokeLater(super.doOKAction())
    }

    lazy val dialog = new MockDialog
  }

}
