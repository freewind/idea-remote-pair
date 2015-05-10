package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core.client.{PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger, TabEventsLocksInProject}
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class HandleFileTabEvents(publishCreateDocumentEvent: PublishCreateDocumentEvent, logger: PluginLogger, publishEvent: PublishEvent, getRelativePath: GetRelativePath, tabEventsLocksInProject: TabEventsLocksInProject, isReadonlyMode: IsReadonlyMode) {

  def handleFileOpened(event: FileOpenedEvent): Unit = {
    publishCreateDocumentEvent(event.file)
  }

  def handleFileClosed(event: FileClosedEvent): Unit = {
    getRelativePath(event.file).foreach(path => publishEvent(CloseTabEvent(path)))
  }

  def handleTabChanged(event: FileTabChangedEvent): Unit = {
    if (!isReadonlyMode()) {
      Option(event.newFile).flatMap(getRelativePath.apply).foreach { p =>
        if (tabEventsLocksInProject.unlock(p)) {
          // do nothing
        } else {
          publishEvent(OpenTabEvent(p))
        }
      }
    }
  }

}
