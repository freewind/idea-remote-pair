package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class HandleClientInfoResponse(currentProject: RichProject) {
  def apply(event: ClientInfoResponse) {
    currentProject.clientInfo = Some(event)
  }
}
