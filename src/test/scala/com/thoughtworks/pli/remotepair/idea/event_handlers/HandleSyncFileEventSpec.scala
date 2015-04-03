package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, SyncFileEvent}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleSyncFileEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleSyncFileEvent = new HandleSyncFileEvent(writeToProjectFile, runWriteAction)

  val event: SyncFileEvent = new SyncFileEvent("from-client-id", "to-client-id", "/file1", new Content("abc", "utf8"))

  "When client get message" should {
    "write message into file" in {
      handleSyncFileEvent(event)

      there was one(writeToProjectFile).apply("/file1", new Content("abc", "utf8"))
    }
  }

}

