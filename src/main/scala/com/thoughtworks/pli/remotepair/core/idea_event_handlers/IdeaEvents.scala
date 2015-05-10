package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

sealed trait IdeaEvent

case class IdeaDocumentChangeEvent(file: VirtualFile, editor: Editor, document: Document) extends IdeaEvent
case class IdeaCaretChangeEvent(file: VirtualFile, editor: Editor, offset: Int) extends IdeaEvent
case class IdeaSelectionChangeEvent(file: VirtualFile, editor: Editor, startOffset: Int, length: Int) extends IdeaEvent
case class IdeaFileDeletedEvent(file: VirtualFile) extends IdeaEvent
case class IdeaFileCreatedEvent(file: VirtualFile) extends IdeaEvent
case class IdeaFileMovedEvent(file: VirtualFile, oldPath: String, newParentPath: String) extends IdeaEvent
case class IdeaFileRenamedEvent(file: VirtualFile, oldName: String) extends IdeaEvent
case class FileOpenedEvent(file: VirtualFile) extends IdeaEvent
case class FileClosedEvent(file: VirtualFile) extends IdeaEvent
case class FileTabChangedEvent(project: Project, oldFile: VirtualFile, oldEditor: FileEditor, newFile: VirtualFile, newEditor: FileEditor) extends IdeaEvent
