package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class ShowErrorDialog(currentProject: IdeaProjectImpl) {
  def apply(title: String = "Error", message: String) = {
    Messages.showMessageDialog(currentProject.rawProject, message, title, Messages.getErrorIcon)
  }
}
