package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, CreateFileEvent}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files.HandleCreateFileEvent
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleCreateFileEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleCreateFileEvent = new HandleCreateFileEvent(runWriteAction, writeToProjectFile, pluginLogger)

  val event = new CreateFileEvent("/abc", Content("hello", "UTF-8"))

  "When client receives CreateFileEvent, it" should {
    "write content to file" in {
      handleCreateFileEvent(event)
      there was one(writeToProjectFile).apply("/abc", Content("hello", "UTF-8"))
    }
  }
}
