package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class ShowMessageDialog(currentProject: Project) {
  def apply(message: String) = {
    Messages.showMessageDialog(currentProject, message, "Information", Messages.getInformationIcon)
  }
}
