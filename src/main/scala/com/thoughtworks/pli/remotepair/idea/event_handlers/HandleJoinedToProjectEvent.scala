package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.models.myfile.GetOpenedFiles
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleJoinedToProjectEvent(getOpenedFiles: GetOpenedFiles, runWriteAction: RunWriteAction, closeFile: CloseFile) {

  def apply(event: JoinedToProjectEvent): Unit = runWriteAction {
    getOpenedFiles().foreach(closeFile.apply)
  }

}
