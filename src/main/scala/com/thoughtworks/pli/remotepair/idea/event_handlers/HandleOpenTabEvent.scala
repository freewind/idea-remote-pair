package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.idea.core._

class HandleOpenTabEvent(getFileByRelative: GetFileByRelative, openTab: OpenTab, tabEventsLocksInProject: TabEventsLocksInProject, getCurrentTimeMillis: GetCurrentTimeMillis, isFileOpened: IsFileOpened) {
  def apply(event: OpenTabEvent) = {
    getFileByRelative(event.path).foreach { file =>
      if (tabEventsLocksInProject.isEmpty && isFileOpened(file)) {
        // do nothing
      } else {
        openTab(file)
        tabEventsLocksInProject.lock(new TabEventLock(event.path, getCurrentTimeMillis()))
      }
    }
  }
}
