package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.{MemoryButton, MemoryFileTree, MemoryList}

class MemoryWatchFilesDialog extends VirtualWatchFilesDialog {
  override def myIde: MyIde = ???
  override def myClient: MyClient = ???
  override def currentProject: MyProject = ???
  override def myUtils: MyUtils = ???
  override def pairEventListeners: PairEventListeners = ???
  override val okButton = new MemoryButton
  override val extraOnCloseHandler: Option[ExtraOnCloseHandler] = None
  override val closeButton = new MemoryButton
  override val watchButton = new MemoryButton
  override val deWatchButton = new MemoryButton
  override val watchingList = new MemoryList
  override val workingTree = new MemoryFileTree
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
}
