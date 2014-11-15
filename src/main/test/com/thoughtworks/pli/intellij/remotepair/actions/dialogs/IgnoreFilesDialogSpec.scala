package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.actions.forms.IgnoreFilesForm
import com.thoughtworks.pli.intellij.remotepair.{IgnoreFilesRequest, PairEvent, ProjectInfoData}
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater

class IgnoreFilesDialogSpec extends Specification with Mockito {

  "IgnoreFilesDialog" should {
    "init the text area with ignore files existing on server" in new Mocking {
      form.getFilesContext.getText === "/aaa\n/bbb"
    }
  }

  "When click on 'OK', it" should {
    "send IgnoreFilesRequest with content of textarea" in new Mocking {
      dialog.doOKAction()
      await()
      there was one(publishEvent).apply(IgnoreFilesRequest(Seq("/aaa", "/bbb")))
    }
    "show error if there is any error when publising" in new Mocking {
      publishEvent.apply(any) throws new RuntimeException("test-error")
      dialog.doOKAction()
      await()
      there was one(showError).apply(any)
    }
  }

  trait Mocking extends Scope {
    m =>
    val project = mock[Project]
    val form = new IgnoreFilesForm
    val publishEvent = mock[PairEvent => Any]
    val showError = mock[String => Any]
    val mockInvokeLater = new MockInvokeLater
    val dialog = new IgnoreFilesDialog(project) {
      override def form = m.form
      override def projectInfo: Option[ProjectInfoData] = Some(ProjectInfoData("any", Nil, Seq("/aaa", "/bbb")))
      override def publishEvent(event: PairEvent): Unit = m.publishEvent.apply(event)
      override def invokeLater(f: => Any): Unit = mockInvokeLater(f)
      override def showError(message: String): Unit = m.showError.apply(message)
    }
    def await() = mockInvokeLater.await()
  }

}
