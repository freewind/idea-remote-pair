package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.idea.core.{DeleteFile, GetFileByRelative, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleDeleteFileEvent(runWriteAction: RunWriteAction, getFileByRelative: GetFileByRelative, deleteFile: DeleteFile, logger: PluginLogger) {

  def apply(event: DeleteFileEvent): Unit = getFileByRelative(event.path) foreach { file =>
    runWriteAction {
      deleteFile(file)
      logger.info("file deleted: " + file)
    }
  }

}
