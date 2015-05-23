package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleMoveDirEvent(currentProject: MyProject, myPlatform: MyIde, logger: PluginLogger) {

  def apply(event: MoveDirEvent): Unit = {
    (currentProject.getFileByRelative(event.path), currentProject.getFileByRelative(event.newParentPath)) match {
      case (Some(dir), Some(newParentFile)) => {
        myPlatform.runWriteAction {
          dir.move(newParentFile)
          logger.info(s"dir moved, ${event.path} -> $dir")
        }
      }
      case _ =>
    }
  }

}
