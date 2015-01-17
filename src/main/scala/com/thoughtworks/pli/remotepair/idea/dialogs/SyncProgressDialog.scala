package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.Component

class SyncProgressDialog(total: Int) extends _SyncProgressDialog {

  progressBar.getModel.setMaximum(total)

  @volatile private var completed: Int = 0

  def completeFile(path: String)(onClose: => Any) = {
    completed += 1
    text.setText(s"$path ($completed/$total)")
    progressBar.getModel.setValue(completed)
    progressBar.updateUI()

    if (completed == total) {
      this.dispose()
      onClose
    }
  }

  def showIt(base: Component) = {
    this.pack()
    this.setSize(400, 100)
    this.setLocationRelativeTo(base)
    this.setVisible(true)
  }
}
