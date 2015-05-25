package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualSyncProgressDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.{MemoryButton, MemoryLabel, MemoryProgressBar}

class MemorySyncProgressDialog extends VirtualSyncProgressDialog {
  override val closeButton = new MemoryButton
  override val progressBar = new MemoryProgressBar
  override val messageLabel = new MemoryLabel
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
  override def pairEventListeners: PairEventListeners = ???
}
