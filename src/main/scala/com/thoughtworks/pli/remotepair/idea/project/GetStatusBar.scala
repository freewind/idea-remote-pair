package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetStatusBar(currentProject: IdeaProjectImpl) {
  def apply(): StatusBar = WindowManager.getInstance().getStatusBar(currentProject.rawProject)
}
