package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.actions.forms.WorkingModeForm
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{PairEvent, CaretSharingModeRequest}

class WorkingModeDialogSpec extends Specification with Mockito {

//  "Initialization" should {
//    "use caret sharing groups from server status to initialize the dialog" in new Mocking {
//      there was one(form).setClientsInCaretSharingMode(Seq(Seq("c1", "c2"), Seq("c3", "c4")))
//    }
//    "use follow mode groups from server status to initialize the dialog" in new Mocking {
//      there was one(form).setClientsInFollowMode(Seq(Seq("f1", "f2"), Seq("f3", "f4")))
//    }
//  }

  "When 'Next' button clicked, it" should {
    "send CaretSharingModeRequest if user selected one radio in caret sharing mode panel" in new Mocking {
//      form.getSelectedClientNameInCaretSharingMode returns Some("p1")
//      dialog.doOKAction()
//      await()
//      there was one(publishEvent).apply(CaretSharingModeRequest("p1"))
      todo
    }
    "send FollowModeRequest if user selected one radio in follow mode panel" in todo
    "send ParallelModelRequest if user selected the parallel mode radio" in todo
  }

  trait Mocking extends Scope {
    self =>
    val project = mock[Project]
    val form = spy(new WorkingModeForm)
    val invokeLater = new MockInvokeLater
    val publishEvent = mock[PairEvent => Unit]
    val showError = mock[String => Any]
    val dialog = new WorkingModeDialog(project) {
      override def form: WorkingModeForm = self.form
      override def invokeLater(f: => Any): Unit = self.invokeLater(f)
      override def publishEvent(event: PairEvent): Unit = self.publishEvent(event)
      override def showError(message: String): Unit = self.showError(message)
    }
    def await() = invokeLater.await()

  }

}
