package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.fileEditor.FileEditorManager
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetFileEditorManager(currentProject: IdeaProjectImpl) {
  def apply(): FileEditorManager = FileEditorManager.getInstance(currentProject.rawProject)
}
