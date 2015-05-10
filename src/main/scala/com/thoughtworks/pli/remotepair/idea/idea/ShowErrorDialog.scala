package com.thoughtworks.pli.remotepair.idea.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class ShowErrorDialog(currentProject: Project) {
  def apply(title: String = "Error", message: String) = {
    Messages.showMessageDialog(currentProject, message, title, Messages.getErrorIcon)
  }
}
