package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateFileEvent(runWriteAction: RunWriteAction, writeToProjectFile: WriteToProjectFile, logger: PluginLogger) {
  def apply(event: CreateFileEvent): Unit = runWriteAction {
    writeToProjectFile(event.path, event.content)
    logger.info(s"file found or created: ${event.path}")
  }
}
