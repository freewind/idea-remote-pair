package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualLabel
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualDifferentFilesTabPanel
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.MemoryLabel

class MemoryDifferentFilesTabPanel extends VirtualDifferentFilesTabPanel {
  override val messageLabel: VirtualLabel = new MemoryLabel
  override def setFiles(files: Seq[String]): Unit = ???
}
