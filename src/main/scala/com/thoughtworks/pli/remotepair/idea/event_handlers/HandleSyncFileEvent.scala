package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFileEvent
import com.thoughtworks.pli.remotepair.idea.core.WriteToProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleSyncFileEvent(writeToProjectFile: WriteToProjectFile, runWriteAction: RunWriteAction) {

  def apply(event: SyncFileEvent): Unit = {
    runWriteAction(writeToProjectFile(event.path, event.content))
  }

}
