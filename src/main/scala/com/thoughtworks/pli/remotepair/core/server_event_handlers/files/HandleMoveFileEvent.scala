package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveFileEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleMoveFileEvent(currentProject: MyProject, myPlatform: MyIde, logger: PluginLogger) {

  def apply(event: MoveFileEvent): Unit = {
    (currentProject.getFileByRelative(event.path), currentProject.getFileByRelative(event.newParentPath)) match {
      case (Some(file), Some(newParentFile)) => {
        myPlatform.runWriteAction {
          file.move(newParentFile)
          logger.info(s"file moved: ${event.path} -> $file")
        }
      }
      case _ =>
    }
  }

}
