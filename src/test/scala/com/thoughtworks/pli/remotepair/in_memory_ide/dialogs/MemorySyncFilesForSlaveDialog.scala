package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.{VirtualPairDifferentFileTabs, VirtualSyncFilesForSlaveDialog}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.MemoryButton

class MemorySyncFilesForSlaveDialog extends VirtualSyncFilesForSlaveDialog {
  override def currentProject: MyProject = ???
  override def myIde: MyIde = ???
  override def myClient: MyClient = ???
  override def dialogFactories: DialogFactories = ???
  override def pairEventListeners: PairEventListeners = ???
  override val okButton = new MemoryButton
  override val cancelButton = new MemoryButton
  override val configButton = new MemoryButton
  override val tabs: VirtualPairDifferentFileTabs = new MemoryPairDifferentFileTabs
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
}
