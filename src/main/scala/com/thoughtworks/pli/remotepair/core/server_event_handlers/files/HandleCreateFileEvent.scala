package com.thoughtworks.pli.remotepair.core.server_event_handlers.files

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}

case class HandleCreateFileEvent(currentProject: MyProject, myIde: MyIde, logger: PluginLogger) {
  def apply(event: CreateFileEvent): Unit = myIde.runWriteAction {
    currentProject.getTextEditorsOfPath(event.path) match {
      case Nil => currentProject.findOrCreateFile(event.path).setContent(event.content.text)
      case editors => editors.foreach(_.document.modifyTo(event.content.text))
    }
    logger.info(s"file updated or created: ${event.path}")
  }
}
