package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core.client.{PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger, TabEventsLocksInProject}

class HandleFileTabEvents(publishCreateDocumentEvent: PublishCreateDocumentEvent, logger: PluginLogger, publishEvent: PublishEvent, tabEventsLocksInProject: TabEventsLocksInProject, isReadonlyMode: IsReadonlyMode) {

  def handleFileOpened(event: FileOpenedEvent): Unit = {
    publishCreateDocumentEvent(event.file)
  }

  def handleFileClosed(event: FileClosedEvent): Unit = {
    event.file.relativePath.foreach(path => publishEvent(CloseTabEvent(path)))
  }

  def handleTabChanged(event: FileTabChangedEvent): Unit = {
    if (!isReadonlyMode()) {
      event.newFile.flatMap(_.relativePath).foreach { p =>
        if (tabEventsLocksInProject.unlock(p)) {
          // do nothing
        } else {
          publishEvent(OpenTabEvent(p))
        }
      }
    }
  }

}
