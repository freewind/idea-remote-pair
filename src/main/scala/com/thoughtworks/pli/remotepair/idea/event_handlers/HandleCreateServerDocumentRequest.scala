package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent
import com.thoughtworks.pli.remotepair.idea.utils.RunReadAction

case class HandleCreateServerDocumentRequest(currentProject: RichProject, runReadAction: RunReadAction, publishEvent: PublishEvent) {
  def apply(request: CreateServerDocumentRequest): Unit = runReadAction {
    currentProject.getFileByRelative(request.path).map(currentProject.getFileContent).foreach { content =>
      publishEvent(CreateDocument(request.path, content))
    }
  }

}
