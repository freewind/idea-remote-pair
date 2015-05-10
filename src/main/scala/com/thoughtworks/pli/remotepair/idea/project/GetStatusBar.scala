package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.{StatusBar, WindowManager}

class GetStatusBar(currentProject: Project) {
  def apply(): StatusBar = WindowManager.getInstance().getStatusBar(currentProject)
}
