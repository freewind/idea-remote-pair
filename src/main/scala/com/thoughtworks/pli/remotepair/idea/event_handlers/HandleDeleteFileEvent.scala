package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.idea.core.DeleteProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleDeleteFileEvent(deleteProjectFile: DeleteProjectFile, runWriteAction: RunWriteAction) {

  def apply(event: DeleteFileEvent): Unit = runWriteAction {
    deleteProjectFile(event.path)
  }

}
