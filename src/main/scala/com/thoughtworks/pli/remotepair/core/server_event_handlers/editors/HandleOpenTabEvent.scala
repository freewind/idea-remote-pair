package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers.{TabEventLock, TabEventsLocksInProject}
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleOpenTabEvent(currentProject: MyProject, tabEventsLocksInProject: TabEventsLocksInProject, mySystem: MySystem, myIde: MyIde) {
  def apply(event: OpenTabEvent) = {
    currentProject.getFileByRelative(event.path).foreach { file =>
      if (tabEventsLocksInProject.isEmpty && file.isActive) {
        // do nothing
      } else {
        myIde.invokeLater {
          currentProject.openFileInTab(file)
        }
        tabEventsLocksInProject.lock(new TabEventLock(event.path, mySystem.now))
      }
    }
  }
}
