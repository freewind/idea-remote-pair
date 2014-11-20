package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.PairEvent
import com.thoughtworks.pli.intellij.remotepair.client.{MockInvokeLater, CurrentProjectHolder}
import org.specs2.mock.Mockito

trait MockCurrentProjectHolder extends Mockito {
  self =>
  val currentProject = mock[Project]

  trait MockCurrentProject extends CurrentProjectHolder {
    override val currentProject = self.currentProject
  }

}

trait DialogMocks extends MockCurrentProjectHolder {

  val publishEvent = mock[PairEvent => Any]
  val showError = mock[String => Any]
  val invokeLater = new MockInvokeLater
}

