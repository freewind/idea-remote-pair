package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleDeleteDirEvent(currentProject: RichProject, runWriteAction: RunWriteAction) {
  def apply(event: DeleteDirEvent): Unit = runWriteAction {
    currentProject.deleteDir(event.path)
  }

}
