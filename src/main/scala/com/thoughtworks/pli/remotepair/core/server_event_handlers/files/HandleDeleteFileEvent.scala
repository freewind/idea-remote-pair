package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.file.DeleteFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleDeleteFileEvent(runWriteAction: RunWriteAction, getFileByRelative: GetFileByRelative, deleteFile: DeleteFile, logger: PluginLogger) {

  def apply(event: DeleteFileEvent): Unit = getFileByRelative(event.path) foreach { file =>
    runWriteAction {
      deleteFile(file)
      logger.info("file deleted: " + file)
    }
  }

}
