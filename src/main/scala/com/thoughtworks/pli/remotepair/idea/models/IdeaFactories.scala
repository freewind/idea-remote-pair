package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.idea.editor.{DrawCaretInEditor, RemoveOldHighlighters, ScrollToCaretInEditor, NewHighlights}

class IdeaFactories(currentProject: IdeaProjectImpl, md5: Md5, newHighlights: NewHighlights, scrollToCaretInEditor: ScrollToCaretInEditor, removeOldHighlighters: RemoveOldHighlighters, drawCaretInEditor: DrawCaretInEditor) {
  def apply(project: Project): IdeaProjectImpl = new IdeaProjectImpl(project)(this)
  def apply(document: Document): IdeaDocumentImpl = new IdeaDocumentImpl(document)
  def apply(editor: Editor): IdeaEditorImpl = new IdeaEditorImpl(editor)(newHighlights, scrollToCaretInEditor, removeOldHighlighters, drawCaretInEditor, this)
  def apply(file: VirtualFile): IdeaFileImpl = new IdeaFileImpl(file, currentProject)(md5, this)
  def platform = new IdeaIdeImpl(currentProject)
}



