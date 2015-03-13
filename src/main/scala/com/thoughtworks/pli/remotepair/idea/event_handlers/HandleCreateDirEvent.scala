package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDirEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateDirEvent(currentProject: RichProject, runWriteAction: RunWriteAction) {
  def apply(event: CreateDirEvent): Unit = runWriteAction {
    currentProject.findOrCreateDir(event.path)
  }

}
