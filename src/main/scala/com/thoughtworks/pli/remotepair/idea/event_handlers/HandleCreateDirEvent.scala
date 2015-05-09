package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDirEvent
import com.thoughtworks.pli.remotepair.idea.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.core.models.myfile.FindOrCreateDir
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDirEvent(findOrCreateDir: FindOrCreateDir, runWriteAction: RunWriteAction, logger: PluginLogger) {

  def apply(event: CreateDirEvent): Unit = runWriteAction {
    findOrCreateDir(event.path)
    logger.info(s"dir found or created: ${event.path}")
  }

}
