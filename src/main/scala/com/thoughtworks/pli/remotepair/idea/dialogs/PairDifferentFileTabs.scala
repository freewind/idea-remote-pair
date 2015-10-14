package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.DefaultListModel

import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.{VirtualDifferentFilesTabPanel, VirtualPairDifferentFileTabs}
import SwingVirtualImplicits._

import scala.language.reflectiveCalls

class PairDifferentFileTabs extends _PairDifferentFileTabs with VirtualPairDifferentFileTabs {
  override def createTabPane() = new DifferentFilesTabPanel()
  override def addTabPanel(pairName: String, tabPane: VirtualDifferentFilesTabPanel): Unit = {
    tabPane match {
      case pane: DifferentFilesTabPanel => _tabs.addTab(pairName, pane.getMainPanel)
      case _ => ???
    }
  }
}

class DifferentFilesTabPanel extends _DifferentFilesTabPane with VirtualDifferentFilesTabPanel {

  override val messageLabel: VirtualLabel = _messageLabel
  override def setFiles(files: Seq[String]): Unit = {
    val model = new DefaultListModel[String]
    for (file <- files) {
      model.addElement(file)
    }
    filesList.setModel(model)
  }
}
