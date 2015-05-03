package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameDirEvent
import com.thoughtworks.pli.remotepair.idea.core.{PluginLogger, GetFileByRelative}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleRenameDirEvent(getFileByRelative: GetFileByRelative, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: RenameDirEvent): Unit = {
    getFileByRelative(event.path).foreach { dir =>
      runWriteAction {
        dir.rename(this, event.newName)
        logger.info(s"file renamed, ${event.path} -> $dir")
      }
    }
  }

}
