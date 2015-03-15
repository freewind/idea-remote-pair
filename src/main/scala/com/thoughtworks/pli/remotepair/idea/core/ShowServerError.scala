package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.ServerErrorResponse

case class ShowServerError(showErrorDialog: ShowErrorDialog) {
  def apply(res: ServerErrorResponse) {
    showErrorDialog("Get error message from server", res.message)
  }
}
