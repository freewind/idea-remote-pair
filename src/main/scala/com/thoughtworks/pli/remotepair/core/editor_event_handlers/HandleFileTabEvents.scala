package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, CreateDocument, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core.{PluginLogger, TabEventsLocksInProject}

class HandleFileTabEvents(logger: PluginLogger, myClient: MyClient, tabEventsLocksInProject: TabEventsLocksInProject) {

  def handleFileOpened(event: EditorFileOpenedEvent): Unit = {
    publishCreateDocumentEvent(event.file)
  }

  def handleFileClosed(event: EditorFileClosedEvent): Unit = {
    event.file.relativePath.foreach(path => myClient.publishEvent(CloseTabEvent(path)))
  }

  def handleTabChanged(event: EditorFileTabChangedEvent): Unit = {
    if (!myClient.isReadonlyMode) {
      event.newFile.flatMap(_.relativePath).foreach { p =>
        if (tabEventsLocksInProject.unlock(p)) {
          // do nothing
        } else {
          myClient.publishEvent(OpenTabEvent(p))
        }
      }
    }
  }

  private def publishCreateDocumentEvent(file: MyFile): Unit = file.relativePath.foreach { path =>
    myClient.publishEvent(CreateDocument(path, file.content))
  }

}
