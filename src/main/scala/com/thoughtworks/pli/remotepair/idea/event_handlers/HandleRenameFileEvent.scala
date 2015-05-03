package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameFileEvent
import com.thoughtworks.pli.remotepair.idea.core.{PluginLogger, GetFileByRelative}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleRenameFileEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: RenameFileEvent): Unit = {
    getFileByRelative(event.path).foreach { file =>
      runWriteAction {
        file.rename(this, event.newName)
        logger.info(s"file renamed, ${event.path} -> $file")
      }
    }
  }

}
