package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.DeleteDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleDeleteDirEvent(currentProject: MyProject, myIde: MyIde, logger: PluginLogger) {

  def apply(event: DeleteDirEvent): Unit = currentProject.getFileByRelative(event.path) foreach { dir =>
    myIde.runWriteAction {
      dir.delete()
      logger.info(s"dir deleted: $dir")
    }
  }

}
