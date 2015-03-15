package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerErrorResponse
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class ShowServerError(currentProject: RichProject) {
  def apply(res: ServerErrorResponse) {
    currentProject.showErrorDialog("Get error message from server", res.message)
  }
}
