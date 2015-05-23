package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.RenameDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleRenameDirEvent(currentProject: MyProject, myPlatform: MyIde, logger: PluginLogger) {

  def apply(event: RenameDirEvent): Unit = {
    currentProject.getFileByRelative(event.path).foreach { dir =>
      myPlatform.runWriteAction {
        dir.rename(event.newName)
        logger.info(s"file renamed, ${event.path} -> $dir")
      }
    }
  }

}
