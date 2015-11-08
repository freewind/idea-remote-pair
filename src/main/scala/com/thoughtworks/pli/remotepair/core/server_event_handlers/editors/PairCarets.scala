package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class PairCarets(currentProject: MyProject, myIde: MyIde, myClient: MyClient) {

  def draw(event: MoveCaretEvent): Unit = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myIde.invokeLater {
        try {
          editor.drawPairCaret(event.offset)
          if (myClient.isCaretSharing) {
            editor.scrollToCaretInEditor(event.offset)
          }
        } catch {
          case e: Throwable =>
        }
      }
    }
  }

  def clearAll(): Unit = {
    currentProject.getAllOpenedTextEditors.foreach(_.clearPairCarets())
  }

}
