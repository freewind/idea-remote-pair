package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualSyncFilesForMasterDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.MemoryButton

class MemorySyncFilesForMasterDialog extends VirtualSyncFilesForMasterDialog {
  override def myClient: MyClient = ???
  override def dialogFactories: DialogFactories = ???
  override val okButton = new MemoryButton
  override val cancelButton = new MemoryButton
  override val configButton = new MemoryButton
  override val tabs = new MemoryPairDifferentFileTabs
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
  override def pairEventListeners: PairEventListeners = ???
}
