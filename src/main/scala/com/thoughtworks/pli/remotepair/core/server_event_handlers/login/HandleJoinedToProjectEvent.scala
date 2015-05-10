package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.idea.file.{CloseFile, GetOpenedFiles}
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleJoinedToProjectEvent(getOpenedFiles: GetOpenedFiles, runWriteAction: RunWriteAction, closeFile: CloseFile) {

  def apply(event: JoinedToProjectEvent): Unit = runWriteAction {
    getOpenedFiles().foreach(closeFile.apply)
  }

}
