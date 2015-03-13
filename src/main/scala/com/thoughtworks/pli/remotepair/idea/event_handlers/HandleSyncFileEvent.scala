package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFileEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleSyncFileEvent(currentProject: RichProject, runWriteAction: RunWriteAction) {
  def apply(event: SyncFileEvent): Unit = {
    runWriteAction(currentProject.smartSetContentTo(event.path, event.content))
  }

}
