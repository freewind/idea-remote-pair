package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleDeleteDirEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(runWriteAction, deleteProjectDir)

  val event = DeleteDirEvent("/abc")
  "When client receives DeleteDirEvent, it" should {
    "delete corresponding dir" in {
      handleDeleteDirEvent(event)
      there was one(deleteProjectDir).apply("/abc")
    }
  }

}
