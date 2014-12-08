package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.actions.forms.{ProjectWithMemberNames, JoinProjectForm}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.ServerStatusResponse
import com.thoughtworks.pli.intellij.remotepair.CreateProjectRequest
import com.thoughtworks.pli.intellij.remotepair.ProjectInfoData

class JoinProjectDialogSpec extends MySpecification {

  "JoinProjectDialog UI" should {
    "use held existing projects to initial the form" in new Mocking {
      invokeLater(dialog).await()
      there was one(form).setExistingProjects(Seq(
        ProjectWithMemberNames("p1", Seq("aa", "bb")),
        ProjectWithMemberNames("p2", Seq("cc", "dd"))))
    }
  }

  "Validation" should {
    "delegate to form's validation when validating" in new Mocking {
      invokeLater(dialog.doValidate()).await()
      there was one(form).validate()
    }
  }

  "When 'Next' button is clicked, it" should {
    "send CreateProjectRequest if user give a new project name" in new Mocking {
      form.getNewProjectName returns Some("p1")
      invokeLater(dialog.doOKAction()).await()
      there was one(publishEvent).apply(CreateProjectRequest("p1", "Freewind"))
    }
    "send JoinProjectRequest if user chose an existing project" in new Mocking {
      form.getExistingProjectName returns Some("p2")
      invokeLater(dialog.doOKAction()).await()
      there was one(publishEvent).apply(JoinProjectRequest("p2", "Freewind"))
    }
    "show error if there is some error when sending request" in new Mocking {
      form.getExistingProjectName returns Some("p2")
      publishEvent.apply(any) throws new RuntimeException("test-error")
      invokeLater(dialog.doOKAction()).await()
      there was one(showError).apply(any)
    }
  }

  trait Mocking extends Scope {
    self =>
    val raw = mock[Project]
    val project = mock[RichProject]
    project.raw returns raw
    project.serverStatus returns Some(ServerStatusResponse(
      Seq(ProjectInfoData("p1", Seq.empty, Nil, WorkingMode.CaretSharing),
        ProjectInfoData("p2", Seq.empty, Nil, WorkingMode.CaretSharing)),
      freeClients = 0
    ))

    val form = spy(new JoinProjectForm)
    form.clientName returns "Freewind"
    val publishEvent = mock[PairEvent => Unit]
    val showError = mock[String => Any]
    val invokeLater = new MockInvokeLater
    val message = None
    lazy val dialog = new JoinProjectDialog(project, message) {
      override def form: JoinProjectForm = self.form
      override def publishEvent(event: PairEvent): Unit = self.publishEvent(event)
      override def invokeLater(f: => Any): Unit = self.invokeLater(f)
      override def showError(message: String): Unit = self.showError.apply(message)
    }
  }

}
