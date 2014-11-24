package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.actions.forms.JoinProjectForm
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
      there was one(form).setExistingProjects(Seq("p1", "p2"))
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
      there was one(publishEvent).apply(CreateProjectRequest("p1"))
    }
    "send JoinProjectRequest if user chose an existing project" in new Mocking {
      form.getExistingProjectName returns Some("p2")
      invokeLater(dialog.doOKAction()).await()
      there was one(publishEvent).apply(JoinProjectRequest("p2"))
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

    val form = spy(new JoinProjectForm)
    val publishEvent = mock[PairEvent => Unit]
    val showError = mock[String => Any]
    val invokeLater = new MockInvokeLater
    lazy val dialog = new JoinProjectDialog(project) {
      override def form: JoinProjectForm = self.form
      override def serverStatus: Option[ServerStatusResponse] = Some(ServerStatusResponse(
        Seq(ProjectInfoData("p1", Seq.empty, Nil), ProjectInfoData("p2", Seq.empty, Nil)),
        Nil
      ))
      override def publishEvent(event: PairEvent): Unit = self.publishEvent(event)
      override def invokeLater(f: => Any): Unit = self.invokeLater(f)
      override def showError(message: String): Unit = self.showError.apply(message)
    }
  }

}
