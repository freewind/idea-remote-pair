package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleRenameFileEvent(currentProject: MyProject, myIde: MyIde, logger: PluginLogger) {

  def apply(event: RenameFileEvent): Unit = {
    currentProject.getFileByRelative(event.path).foreach { file =>
      myIde.runWriteAction {
        file.rename(event.newName)
        logger.info(s"file renamed, ${event.path} -> $file")
      }
    }
  }

}
