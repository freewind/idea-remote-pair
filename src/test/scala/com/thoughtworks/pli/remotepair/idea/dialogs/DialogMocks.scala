package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import com.thoughtworks.pli.remotepair.idea.core.{RichProject, CurrentProjectHolder}
import org.specs2.mock.Mockito

trait MockCurrentProjectHolder extends Mockito {
  self =>
  val currentProject = mock[RichProject]

  trait MockCurrentProject extends CurrentProjectHolder {
    override val currentProject = self.currentProject
  }

}

trait DialogMocks extends MockCurrentProjectHolder {

  val publishEvent = mock[PairEvent => Any]
  val showError = mock[String => Any]
  val invokeLater = new MockInvokeLater
}

