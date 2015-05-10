package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleRenameDirEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: RenameDirEvent): Unit = {
    getFileByRelative(event.path).foreach { dir =>
      runWriteAction {
        dir.rename(event.newName)
        logger.info(s"file renamed, ${event.path} -> $dir")
      }
    }
  }

}
