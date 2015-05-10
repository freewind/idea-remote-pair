package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleMoveFileEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: MoveFileEvent): Unit = {
    (getFileByRelative(event.path), getFileByRelative(event.newParentPath)) match {
      case (Some(file), Some(newParentFile)) => {
        runWriteAction {
          file.move(this, newParentFile)
          logger.info(s"file moved: ${event.path} -> $file")
        }
      }
      case _ =>
    }
  }

}
