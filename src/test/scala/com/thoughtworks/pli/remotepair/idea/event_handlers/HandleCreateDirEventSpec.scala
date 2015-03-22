package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDirEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleCreateDirEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleCreateDirEvent = new HandleCreateDirEvent(findOrCreateDir, runWriteAction)

  val event = CreateDirEvent("/abc")

  "When client receives CreateDirEvent, it" should {
    "create corresponding dir" in {
      handleCreateDirEvent(event)
      there was one(findOrCreateDir).apply("/abc")
    }
  }
}

