package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class RichProjectSpec extends Specification with Mockito {

  "2 rich projects" should {
    "equal with each other if the raw projects are equal" in {
      val raw = mock[Project]
      val project1 = new RichProject(raw)
      val project2 = new RichProject(raw)
      project1 === project2
    }
  }
}
