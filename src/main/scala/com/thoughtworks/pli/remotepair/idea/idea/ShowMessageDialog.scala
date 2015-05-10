package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class ShowMessageDialog(currentProject: IdeaProjectImpl) {
  def apply(message: String) = {
    Messages.showMessageDialog(currentProject.raw, message, "Information", Messages.getInformationIcon)
  }
}
