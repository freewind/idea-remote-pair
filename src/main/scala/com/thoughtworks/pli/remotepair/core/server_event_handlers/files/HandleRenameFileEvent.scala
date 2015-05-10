package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}

class HandleRenameFileEvent(currentProject: MyProject, myPlatform: MyPlatform, logger: PluginLogger) {

  def apply(event: RenameFileEvent): Unit = {
    currentProject.getFileByRelative(event.path).foreach { file =>
      myPlatform.runWriteAction {
        file.rename(event.newName)
        logger.info(s"file renamed, ${event.path} -> $file")
      }
    }
  }

}
