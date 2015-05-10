package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.idea.file.IsFileInActiveTab
import com.thoughtworks.pli.remotepair.idea.idea.OpenFileInTab
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative

class HandleOpenTabEvent(getFileByRelative: GetFileByRelative, openTab: OpenFileInTab, tabEventsLocksInProject: TabEventsLocksInProject, getCurrentTimeMillis: GetCurrentTimeMillis, isFileInActiveTab: IsFileInActiveTab) {
  def apply(event: OpenTabEvent) = {
    getFileByRelative(event.path).foreach { file =>
      if (tabEventsLocksInProject.isEmpty && isFileInActiveTab(file)) {
        // do nothing
      } else {
        openTab(file)
        tabEventsLocksInProject.lock(new TabEventLock(event.path, getCurrentTimeMillis()))
      }
    }
  }
}
