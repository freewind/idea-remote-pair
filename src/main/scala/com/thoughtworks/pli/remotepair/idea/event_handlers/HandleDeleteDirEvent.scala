package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.idea.core.DeleteProjectDir
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleDeleteDirEvent(runWriteAction: RunWriteAction, deleteProjectDir: DeleteProjectDir) {
  def apply(event: DeleteDirEvent): Unit = runWriteAction {
    deleteProjectDir(event.path)
  }

}
