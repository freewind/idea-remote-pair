package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}

class HandleMoveCaretEvent(currentProject: MyProject, myPlatform: MyPlatform, connectedClient: ConnectedClient) {

  def apply(event: MoveCaretEvent): Unit = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myPlatform.invokeLater {
        try {
          editor.drawCaretInEditor(event.offset)
          if (connectedClient.isCaretSharing) {
            editor.scrollToCaretInEditor(event.offset)
          }
        } catch {
          case e: Throwable =>
        }
      }
    }
  }

}
