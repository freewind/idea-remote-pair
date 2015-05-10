package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files.HandleDeleteFileEvent
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleDeleteFileEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(runWriteAction, getFileByRelative, deleteFile, pluginLogger)

  val event = DeleteFileEvent("/abc")
  val file = mock[VirtualFile]
  getFileByRelative("/abc") returns Some(file)

  "When client receives DeleteFileEvent, it" should {
    "delete corresponding file" in {
      handleDeleteFileEvent(event)
      there was one(deleteFile).apply(file)
    }
  }

}
