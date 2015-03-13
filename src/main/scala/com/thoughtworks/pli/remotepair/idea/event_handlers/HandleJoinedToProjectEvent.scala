package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishCreateDocumentEvent

case class HandleJoinedToProjectEvent(currentProject: RichProject, publishCreateDocumentEvent: PublishCreateDocumentEvent) {
  def apply(event: JoinedToProjectEvent): Unit = {
    currentProject.getOpenedFiles.foreach(publishCreateDocumentEvent.apply)
  }

}
