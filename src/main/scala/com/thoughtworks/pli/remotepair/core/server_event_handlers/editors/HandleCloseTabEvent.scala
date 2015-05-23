package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.CloseTabEvent
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleCloseTabEvent(currentProject: MyProject, myPlatform: MyIde) {

  def apply(event: CloseTabEvent) = {
    currentProject.getFileByRelative(event.path).foreach(file => myPlatform.invokeLater(file.close()))
  }

}
