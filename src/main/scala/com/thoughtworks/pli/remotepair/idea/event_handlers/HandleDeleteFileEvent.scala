package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleDeleteFileEvent(currentProject: RichProject, runWriteAction: RunWriteAction) {
  def apply(event: DeleteFileEvent): Unit = runWriteAction {
    currentProject.deleteFile(event.path)
  }

}
