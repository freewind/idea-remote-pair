package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class GetFileEditorManager(currentProject: Project) {
  def apply(): FileEditorManager = FileEditorManager.getInstance(currentProject)
}
