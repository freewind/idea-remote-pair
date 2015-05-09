package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager

class GetProjectWindow(currentProject: Project) {
  def apply() = WindowManager.getInstance().getFrame(currentProject)
}
