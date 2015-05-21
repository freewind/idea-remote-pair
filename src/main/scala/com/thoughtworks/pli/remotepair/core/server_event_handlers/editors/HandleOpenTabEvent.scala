package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}

class HandleOpenTabEvent(currentProject: MyProject, tabEventsLocksInProject: TabEventsLocksInProject, getCurrentTimeMillis: GetCurrentTimeMillis, myPlatform: MyPlatform) {
  def apply(event: OpenTabEvent) = {
    currentProject.getFileByRelative(event.path).foreach { file =>
      if (tabEventsLocksInProject.isEmpty && file.isActive) {
        // do nothing
      } else {
        myPlatform.invokeLater {
          currentProject.openFileInTab(file)
        }
        tabEventsLocksInProject.lock(new TabEventLock(event.path, getCurrentTimeMillis()))
      }
    }
  }
}