package com.thoughtworks.pli.remotepair.core.editor_event_handlers

class HandleIdeaEvent(handleCaretChangeEvent: HandleCaretChangeEvent,
                      handleDocumentChangeEvent: HandleDocumentChangeEvent,
                      handleFileTabEvents: HandleFileTabEvents,
                      handleIdeaFileEvent: HandleIdeaFileEvent,
                      handleSelectionEvent: HandleSelectionEvent) {

  def apply(ideaEvent: EditorEvent): Unit = ideaEvent match {
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



