package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleMoveDirEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: MoveDirEvent): Unit = {
    (getFileByRelative(event.path), getFileByRelative(event.newParentPath)) match {
      case (Some(dir), Some(newParentFile)) => {
        runWriteAction {
          dir.move(this, newParentFile)
          logger.info(s"dir moved, ${event.path} -> $dir")
        }
      }
      case _ =>
    }
  }

}
