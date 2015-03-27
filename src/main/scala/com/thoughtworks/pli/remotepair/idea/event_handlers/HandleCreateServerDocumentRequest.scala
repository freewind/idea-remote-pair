package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}
import com.thoughtworks.pli.remotepair.idea.core.{GetFileContent, GetFileByRelative, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.utils.RunReadAction

case class HandleCreateServerDocumentRequest(runReadAction: RunReadAction, publishEvent: PublishEvent, getFileByRelative: GetFileByRelative, getFileContent: GetFileContent) {

  def apply(request: CreateServerDocumentRequest): Unit = runReadAction {
    getFileByRelative(request.path).map(getFileContent.apply).foreach { content =>
      publishEvent(CreateDocument(request.path, content))
    }
  }

}
