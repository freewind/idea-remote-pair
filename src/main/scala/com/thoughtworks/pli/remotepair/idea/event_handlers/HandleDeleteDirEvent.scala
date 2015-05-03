package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.idea.core.{PluginLogger, DeleteFile, GetFileByRelative}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleDeleteDirEvent(runWriteAction: RunWriteAction, getFileByRelative: GetFileByRelative, deleteFile: DeleteFile, logger: PluginLogger) {

  def apply(event: DeleteDirEvent): Unit = getFileByRelative(event.path) foreach { dir =>
    runWriteAction {
      deleteFile(dir)
      logger.info(s"dir deleted: $dir")
    }
  }

}
