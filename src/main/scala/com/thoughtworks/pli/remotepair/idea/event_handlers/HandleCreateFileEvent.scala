package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.WriteToProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateFileEvent(runWriteAction: RunWriteAction, writeToProjectFile: WriteToProjectFile) {
  def apply(event: CreateFileEvent): Unit = runWriteAction {
    writeToProjectFile(event.path, event.content)
  }
}
