package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDirEvent
import com.thoughtworks.pli.remotepair.idea.core.FindOrCreateDir
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDirEvent(findOrCreateDir: FindOrCreateDir, runWriteAction: RunWriteAction) {

  def apply(event: CreateDirEvent): Unit = runWriteAction {
    findOrCreateDir(event.path)
  }

}
