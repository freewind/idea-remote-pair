package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.remotepair.core.models.{MyDocument, MyEditor, MyFile}

sealed trait EditorEvent

case class EditorDocumentChangeEvent(file: MyFile, editor: MyEditor, document: MyDocument) extends EditorEvent
case class EditorCaretChangeEvent(file: MyFile, editor: MyEditor, offset: Int) extends EditorEvent
case class EditorSelectionChangeEvent(file: MyFile, editor: MyEditor, startOffset: Int, length: Int) extends EditorEvent
case class EditorFileDeletedEvent(file: MyFile) extends EditorEvent
case class EditorFileCreatedEvent(file: MyFile) extends EditorEvent
case class EditorFileMovedEvent(file: MyFile, oldPath: String, newParentPath: String) extends EditorEvent
case class EditorFileRenamedEvent(file: MyFile, oldName: String) extends EditorEvent
case class EditorFileOpenedEvent(file: MyFile) extends EditorEvent
case class EditorFileClosedEvent(file: MyFile) extends EditorEvent
case class EditorFileTabChangedEvent(oldFile: Option[MyFile], newFile: Option[MyFile]) extends EditorEvent
