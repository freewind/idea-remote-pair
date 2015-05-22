package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}

case class HandleCreateServerDocumentRequest(currentProject: MyProject, myPlatform: MyPlatform, connectedClient: ConnectedClient) {

  def apply(request: CreateServerDocumentRequest): Unit = myPlatform.runReadAction {
    currentProject.getFileByRelative(request.path).map(_.content).foreach { content =>
      connectedClient.publishEvent(CreateDocument(request.path, content))
    }
  }

}
