package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.wm.WindowManager
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class GetProjectWindow(currentProject: IdeaProjectImpl) {
  def apply() = WindowManager.getInstance().getFrame(currentProject.raw)
}
