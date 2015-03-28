package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.idea.core.TabEventLock
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleOpenTabEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleOpenTabEvent = new HandleOpenTabEvent(getFileByRelative, openTab, tabEventsLocksInProject, getCurrentTimeMillis, isFileOpened)

  val file = mock[VirtualFile]
  val event = new OpenTabEvent("/abc")
  getFileByRelative.apply("/abc") returns Some(file)
  getCurrentTimeMillis.apply() returns 123456L
  tabEventsLocksInProject.isEmpty returns false
  isFileOpened.apply(file) returns false

  "When client receives OpenTabEvent, it" should {
    "open corresponding tab and hold the path & currentTime in project for later use" in {
      handleOpenTabEvent(event)
      there was one(openTab).apply(file)
      there was one(tabEventsLocksInProject).lock(TabEventLock("/abc", 123456L))
    }
    "not do anything if the asked file is opened and there is no lock existed" in {
      isFileOpened.apply(file) returns true
      tabEventsLocksInProject.isEmpty returns true
      handleOpenTabEvent(event)
      there was no(openTab).apply(any)
      there was no(tabEventsLocksInProject).lock(any)
    }
  }

}
