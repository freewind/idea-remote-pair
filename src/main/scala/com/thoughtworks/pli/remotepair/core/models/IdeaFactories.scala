package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.MyUtils

class IdeaFactories(currentProject: MyProject, myUtils: MyUtils) {
  def apply(project: Project): MyProject = new MyProject(project)(this)
  def apply(document: Document): MyDocument = new MyDocument(document)
  def apply(editor: Editor): MyEditor = new MyEditor(editor)(this)
  def apply(file: VirtualFile): MyFile = new MyFile(file, currentProject)(myUtils, this)
  def platform = new MyIde(currentProject)
}



