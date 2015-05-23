package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.MyUtils

class IdeaFactories(currentProject: IdeaProjectImpl, myUtils: MyUtils) {
  def apply(project: Project): IdeaProjectImpl = new IdeaProjectImpl(project)(this)
  def apply(document: Document): IdeaDocumentImpl = new IdeaDocumentImpl(document)
  def apply(editor: Editor): IdeaEditorImpl = new IdeaEditorImpl(editor)(this)
  def apply(file: VirtualFile): IdeaFileImpl = new IdeaFileImpl(file, currentProject)(myUtils, this)
  def platform = new IdeaIdeImpl(currentProject)
}



