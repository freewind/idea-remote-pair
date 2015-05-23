package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile

case class HandleCreateFileEvent(myPlatform: MyIde, writeToProjectFile: WriteToProjectFile, logger: PluginLogger) {
  def apply(event: CreateFileEvent): Unit = myPlatform.runWriteAction {
    writeToProjectFile(event.path, event.content)
    logger.info(s"file found or created: ${event.path}")
  }
}
