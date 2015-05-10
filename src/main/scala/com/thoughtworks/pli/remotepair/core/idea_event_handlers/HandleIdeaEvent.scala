package com.thoughtworks.pli.remotepair.core.idea_event_handlers

class HandleIdeaEvent(handleCaretChangeEvent: HandleCaretChangeEvent,
                      handleDocumentChangeEvent: HandleDocumentChangeEvent,
                      handleFileTabEvents: HandleFileTabEvents,
                      handleIdeaFileEvent: HandleIdeaFileEvent,
                      handleSelectionEvent: HandleSelectionEvent) {

  def apply(ideaEvent: IdeaEvent): Unit = ideaEvent match {
    case event: IdeaCaretChangeEvent => handleCaretChangeEvent(event)
    case event: FileClosedEvent => handleFileTabEvents.handleFileClosed(event)
    case event: FileOpenedEvent => handleFileTabEvents.handleFileOpened(event)
    case event: FileTabChangedEvent => handleFileTabEvents.handleTabChanged(event)
    case event: IdeaDocumentChangeEvent => handleDocumentChangeEvent(event)
    case event: IdeaFileCreatedEvent => handleIdeaFileEvent.handleFileCreated(event)
    case event: IdeaFileDeletedEvent => handleIdeaFileEvent.handleFileDeleted(event)
    case event: IdeaFileMovedEvent => handleIdeaFileEvent.handleFileMoved(event)
    case event: IdeaFileRenamedEvent => handleIdeaFileEvent.handleFileRenamed(event)
    case event: IdeaSelectionChangeEvent => handleSelectionEvent(event)
  }

}



