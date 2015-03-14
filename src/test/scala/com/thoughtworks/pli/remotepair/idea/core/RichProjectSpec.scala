package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class RichProjectSpec extends Specification with Mockito with MocksModule {
  override lazy val richProjectFactory: RichProjectFactory = wire[RichProjectFactory]

  "2 rich projects" should {
    "equal with each other if the raw projects are equal" in {
      val raw = mock[Project]
      val project1 = richProjectFactory.create(raw)
      val project2 = richProjectFactory.create(raw)
      project1 === project2
    }
  }
}
