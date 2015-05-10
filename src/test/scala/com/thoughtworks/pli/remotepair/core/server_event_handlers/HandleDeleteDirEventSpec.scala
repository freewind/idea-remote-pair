package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files.HandleDeleteDirEvent
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleDeleteDirEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(runWriteAction, getFileByRelative, deleteFile, pluginLogger)

  val event = DeleteDirEvent("/abc")
  val file = mock[VirtualFile]
  getFileByRelative.apply("/abc") returns Some(file)

  "When client receives DeleteDirEvent, it" should {
    "delete corresponding dir" in {
      handleDeleteDirEvent(event)
      there was one(deleteFile).apply(file)
    }
  }

}
