package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameDirEvent
import com.thoughtworks.pli.remotepair.idea.core.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleRenameDirEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction) {

  def apply(event: RenameDirEvent): Unit = {
    getFileByRelative(event.path).foreach { fromFile =>
      runWriteAction {
        fromFile.rename(this, event.newName)
      }
    }
  }

}
