package com.thoughtworks.pli.remotepair.in_memory_ide

import java.io.File

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class MyTest extends Specification with Mockito with MemoryTestModule {
  isolated

  override val projectRoot = new File("./src/test/resources/test-project-root")

  "something" should {
    "do something" in {
      project.openFile("README.md")
      println(project._editors)
      println(project.getTextEditorsOfPath("README.md"))
      true
    }
  }

}

