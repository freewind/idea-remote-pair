package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFileEvent
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

class HandleSyncFileEvent(currentProject: MyProject, myIde: MyIde) {

  def apply(event: SyncFileEvent): Unit = {
    myIde.runWriteAction {
      currentProject.getTextEditorsOfPath(event.path) match {
        case Nil => currentProject.findOrCreateFile(event.path).setContent(event.content.text)
        case editors => editors.foreach(_.document.modifyTo(event.content.text))
      }
    }
  }

}
