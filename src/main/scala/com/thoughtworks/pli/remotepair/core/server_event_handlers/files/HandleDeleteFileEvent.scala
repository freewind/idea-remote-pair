package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleDeleteFileEvent(currentProject: MyProject, myIde: MyIde, logger: PluginLogger) {

  def apply(event: DeleteFileEvent): Unit = currentProject.getFileByRelative(event.path) foreach { file =>
    myIde.runWriteAction {
      file.delete()
      logger.info("file deleted: " + file)
    }
  }

}
