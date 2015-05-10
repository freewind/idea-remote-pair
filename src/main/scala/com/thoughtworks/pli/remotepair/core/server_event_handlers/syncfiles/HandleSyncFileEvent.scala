package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFileEvent
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile

class HandleSyncFileEvent(writeToProjectFile: WriteToProjectFile, myPlatform: MyPlatform) {

  def apply(event: SyncFileEvent): Unit = {
    myPlatform.runWriteAction(writeToProjectFile(event.path, event.content))
  }

}
