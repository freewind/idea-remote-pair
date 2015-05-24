package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, CreateServerDocumentRequest}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

case class HandleCreateServerDocumentRequest(currentProject: MyProject, myIde: MyIde, myClient: MyClient) {

  def apply(request: CreateServerDocumentRequest): Unit = myIde.runReadAction {
    currentProject.getFileByRelative(request.path).map(_.content).foreach { content =>
      myClient.publishEvent(CreateDocument(request.path, content))
    }
  }

}
