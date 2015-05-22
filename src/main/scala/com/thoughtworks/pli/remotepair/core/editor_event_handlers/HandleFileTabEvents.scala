package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core.client.{ConnectedClient, PublishCreateDocumentEvent}
import com.thoughtworks.pli.remotepair.core.{PluginLogger, TabEventsLocksInProject}

class HandleFileTabEvents(publishCreateDocumentEvent: PublishCreateDocumentEvent, logger: PluginLogger, connectedClient: ConnectedClient, tabEventsLocksInProject: TabEventsLocksInProject) {

  def handleFileOpened(event: EditorFileOpenedEvent): Unit = {
    publishCreateDocumentEvent(event.file)
  }

  def handleFileClosed(event: EditorFileClosedEvent): Unit = {
    event.file.relativePath.foreach(path => connectedClient.publishEvent(CloseTabEvent(path)))
  }

  def handleTabChanged(event: EditorFileTabChangedEvent): Unit = {
    if (!connectedClient.isReadonlyMode) {
      event.newFile.flatMap(_.relativePath).foreach { p =>
        if (tabEventsLocksInProject.unlock(p)) {
          // do nothing
        } else {
          connectedClient.publishEvent(OpenTabEvent(p))
        }
      }
    }
  }

}
