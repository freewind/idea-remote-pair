package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualLabel

trait VirtualDifferentFilesTabPanel {
  val messageLabel: VirtualLabel
  def setFiles(files: Seq[String])
}
