package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.remotepair.core.models.{MyDocument, MyEditor, MyFile}

sealed trait IdeaEvent

case class IdeaDocumentChangeEvent(file: MyFile, editor: MyEditor, document: MyDocument) extends IdeaEvent
case class IdeaCaretChangeEvent(file: MyFile, editor: MyEditor, offset: Int) extends IdeaEvent
case class IdeaSelectionChangeEvent(file: MyFile, editor: MyEditor, startOffset: Int, length: Int) extends IdeaEvent
case class IdeaFileDeletedEvent(file: MyFile) extends IdeaEvent
case class IdeaFileCreatedEvent(file: MyFile) extends IdeaEvent
case class IdeaFileMovedEvent(file: MyFile, oldPath: String, newParentPath: String) extends IdeaEvent
case class IdeaFileRenamedEvent(file: MyFile, oldName: String) extends IdeaEvent
case class FileOpenedEvent(file: MyFile) extends IdeaEvent
case class FileClosedEvent(file: MyFile) extends IdeaEvent
case class FileTabChangedEvent(oldFile: Option[MyFile], newFile: Option[MyFile]) extends IdeaEvent
