package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleRenameFileEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: RenameFileEvent): Unit = {
    getFileByRelative(event.path).foreach { file =>
      runWriteAction {
        file.rename(event.newName)
        logger.info(s"file renamed, ${event.path} -> $file")
      }
    }
  }

}
