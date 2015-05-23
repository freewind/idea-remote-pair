package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDirEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

case class HandleCreateDirEvent(currentProject: MyProject, myPlatform: MyIde, logger: PluginLogger) {

  def apply(event: CreateDirEvent): Unit = myPlatform.runWriteAction {
    currentProject.findOrCreateDir(event.path)
    logger.info(s"dir found or created: ${event.path}")
  }

}
