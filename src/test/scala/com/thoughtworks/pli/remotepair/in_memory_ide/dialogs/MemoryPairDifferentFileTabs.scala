package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.ui.dialogs.{VirtualDifferentFilesTabPanel, VirtualPairDifferentFileTabs}

class MemoryPairDifferentFileTabs extends VirtualPairDifferentFileTabs {
  override def createTabPane(): VirtualDifferentFilesTabPanel = ???
  override def addTabPanel(pairName: String, tabPane: VirtualDifferentFilesTabPanel): Unit = ???
}
