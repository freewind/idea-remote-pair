package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleDeleteFileEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(deleteProjectFile, runWriteAction)

  val event = DeleteFileEvent("/abc")

  "When client receives DeleteFileEvent, it" should {
    "delete corresponding file" in {
      handleDeleteFileEvent(event)
      there was one(deleteProjectFile).apply("/abc")
    }
  }

}
