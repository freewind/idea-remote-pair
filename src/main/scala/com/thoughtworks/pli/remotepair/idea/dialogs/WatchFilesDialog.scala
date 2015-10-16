package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyIde}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import SwingVirtualImplicits._

class WatchFilesDialog(val extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, val myClient: MyClient, val pairEventListeners: PairEventListeners, val currentProject: MyProject, val myUtils: MyUtils)
  extends _WatchFilesDialog with JDialogSupport with VirtualWatchFilesDialog {

  override val okButton: VirtualButton = _okButton
  override val closeButton: VirtualButton = _closeButton
  override val watchButton: VirtualButton = _watchButton
  override val deWatchButton: VirtualButton = _deWatchButton
  override val workingTree: VirtualFileTree = _workingTree
  override val watchingList: VirtualList = _watchingList
  override val dialog: VirtualDialog = this

  setSize(Size(600, 400))
  init()

}
