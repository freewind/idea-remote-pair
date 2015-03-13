package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class HandleServerStatusResponse(currentProject: RichProject) {
  def apply(res: ServerStatusResponse) {
    currentProject.serverStatus = Some(res)
  }
}
