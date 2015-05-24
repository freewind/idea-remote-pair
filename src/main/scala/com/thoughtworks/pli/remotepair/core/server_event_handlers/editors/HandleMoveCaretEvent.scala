package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleMoveCaretEvent(currentProject: MyProject, myIde: MyIde, myClient: MyClient) {

  def apply(event: MoveCaretEvent): Unit = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myIde.invokeLater {
        try {
          editor.drawCaretInEditor(event.offset)
          if (myClient.isCaretSharing) {
            editor.scrollToCaretInEditor(event.offset)
          }
        } catch {
          case e: Throwable =>
        }
      }
    }
  }

}
