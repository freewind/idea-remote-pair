package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualCopyProjectUrlDialog
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.{MemoryButton, MemoryInputField}

class MemoryCopyProjectUrlDialog extends VirtualCopyProjectUrlDialog {
  override def myProjectStorage: MyProjectStorage = ???
  override def mySystem: MySystem = ???
  override def logger: PluginLogger = ???
  override val copyAndCloseButton = new MemoryButton
  override val projectUrlField = new MemoryInputField
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
  override def pairEventListeners: PairEventListeners = ???
}
