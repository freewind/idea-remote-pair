package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.actions.forms.IgnoreFilesForm
import com.thoughtworks.pli.intellij.remotepair.{IgnoreFilesRequest, PairEvent, ProjectInfoData}
import org.specs2.specification.Scope

class IgnoreFilesDialogSpec extends MySpecification {

  "IgnoreFilesDialog" should {
    "init the text area with ignore files existing on server" in new Mocking {
      invokeLater(newDialog).await()
      form.getFilesContext.getText === "/aaa\n/bbb"
    }
  }

  "When click on 'OK', it" should {
    "send IgnoreFilesRequest with content of textarea" in new Mocking {
      invokeLater(newDialog.doOKAction()).await()
      there was one(publishEvent).apply(IgnoreFilesRequest(Seq("/aaa", "/bbb")))
    }
    "show error if there is any error when publishing" in new Mocking {
      publishEvent.apply(any) throws new RuntimeException("test-error")
      invokeLater(newDialog.doOKAction()).await()
      there was one(showError).apply(any)
    }
  }

  trait Mocking extends Scope with DialogMocks {
    m =>
    val form = new IgnoreFilesForm with MockCurrentProject

    def newDialog = new IgnoreFilesDialog(currentProject) {
      override def form = m.form
      override def projectInfo = Some(ProjectInfoData("any", Nil, Seq("/aaa", "/bbb")))
      override def publishEvent(event: PairEvent) = m.publishEvent.apply(event)
      override def invokeLater(f: => Any) = m.invokeLater(f)
      override def showError(message: String) = m.showError.apply(message)
    }
  }

}
