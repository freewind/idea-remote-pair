//package com.thoughtworks.pli.intellij.remotepair.actions.forms
//
//import com.thoughtworks.pli.intellij.remotepair.MySpecification
//import org.specs2.specification.Scope
//
//class JoinProjectFormSpec extends MySpecification {
//
//  "JoinProjectForm" should {
//    "allow to create a group of radios for existing projects" in new Mocking {
//      form.existingProjectRadios must have size 2
//    }
//    "get correct project name if user chose an existing project" in new Mocking {
//      form.existingProjectRadios.last.setSelected(true)
//      form.selectedExistingProject === Some("p2")
//    }
//    "input and get new project name" in new Mocking {
//      form.getTxtNewProjectName.setText("new-project")
//      form.newProjectName === Some("new-project")
//    }
//    "trim the project name when getting" in new Mocking {
//      form.getTxtNewProjectName.setText("   new-project  ")
//      form.newProjectName === Some("new-project")
//    }
//  }
//
//  "'Create project' checkbox" should {
//    "when checked, it" should {
//      "disable the radios of existing projects" in new Mocking {
//        form.getRadioNewProject.setSelected(true)
//        form.existingProjectRadios.exists(_.isEnabled) === false
//      }
//      "enable the 'New project name' text field" in new Mocking {
//        form.getRadioNewProject.setSelected(true)
//        form.getTxtNewProjectName.isEnabled === true
//      }
//    }
//    "when not checked, it" should {
//      "disable the 'New project name' input field" in new Mocking {
//        todo
//      }
//      "enable the radios of existing projects" in new Mocking {
//        todo
//      }
//    }
//  }
//
//  "Validation" should {
//    "be invalid if user has not choose any radio" in todo
//    "be invalid if user chose 'Create project' but didn't input a project name" in todo
//
//    "be valid if user chose any existing project" in todo
//    "be valid if user chose 'Create project' and give a non-empty project name" in todo
//  }
//
//  trait Mocking extends Scope {
//    val form = new JoinProjectForm
//    form.setExistingProjects(Seq(
//      ProjectWithMemberNames("p1", Seq("aa", "bb")),
//      ProjectWithMemberNames("p2", Seq("cc", "dd"))))
//  }
//
//}
