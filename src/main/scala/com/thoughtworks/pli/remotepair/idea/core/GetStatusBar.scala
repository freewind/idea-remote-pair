package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.{WindowManager, StatusBar}

class GetStatusBar(currentProject: Project) {
  def apply(): StatusBar = WindowManager.getInstance().getStatusBar(currentProject)
}
