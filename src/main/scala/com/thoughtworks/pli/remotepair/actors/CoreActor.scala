package com.thoughtworks.pli.remotepair.actors

import akka.actor.Actor
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._

class CoreActor(handleCaretChangeEvent: HandleCaretChangeEvent,
                handleDocumentChangeEvent: HandleDocumentChangeEvent,
                handleFileTabEvents: HandleFileTabEvents,
                handleIdeaFileEvent: HandleIdeaFileEvent,
                handleSelectionEvent: HandleSelectionEvent) extends Actor {

  override def receive: Receive = {
    case event: EditorCaretChangeEvent => handleCaretChangeEvent(event)
    case event: EditorFileClosedEvent => handleFileTabEvents.handleFileClosed(event)
    case event: EditorFileOpenedEvent => handleFileTabEvents.handleFileOpened(event)
    case event: EditorFileTabChangedEvent => handleFileTabEvents.handleTabChanged(event)
    case event: EditorDocumentChangeEvent => handleDocumentChangeEvent(event)
    case event: EditorFileCreatedEvent => handleIdeaFileEvent.handleFileCreated(event)
    case event: EditorFileDeletedEvent => handleIdeaFileEvent.handleFileDeleted(event)
    case event: EditorFileMovedEvent => handleIdeaFileEvent.handleFileMoved(event)
    case event: EditorFileRenamedEvent => handleIdeaFileEvent.handleFileRenamed(event)
    case event: EditorSelectionChangeEvent => handleSelectionEvent(event)
  }

}



