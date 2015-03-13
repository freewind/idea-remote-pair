package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleCreateFileEvent(val currentProject: RichProject, runWriteAction: RunWriteAction) {
  def apply(event: CreateFileEvent): Unit = runWriteAction {
    currentProject.smartSetContentTo(event.path, event.content)
  }
}
