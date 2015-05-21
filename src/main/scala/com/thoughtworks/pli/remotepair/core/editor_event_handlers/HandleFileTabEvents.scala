package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core.client.{PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger, TabEventsLocksInProject}

class HandleFileTabEvents(publishCreateDocumentEvent: PublishCreateDocumentEvent, logger: PluginLogger, publishEvent: PublishEvent, tabEventsLocksInProject: TabEventsLocksInProject, isReadonlyMode: IsReadonlyMode) {

  def handleFileOpened(event: EditorFileOpenedEvent): Unit = {
    publishCreateDocumentEvent(event.file)
  }

  def handleFileClosed(event: EditorFileClosedEvent): Unit = {
    event.file.relativePath.foreach(path => publishEvent(CloseTabEvent(path)))
  }

  def handleTabChanged(event: EditorFileTabChangedEvent): Unit = {
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
