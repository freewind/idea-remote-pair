package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
  type Factory = Option[ExtraOnCloseHandler] => WatchFilesDialog
}

class WatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, val myClient: MyClient, val pairEventListeners: PairEventListeners, val currentProject: IdeaProjectImpl, val myUtils: MyUtils)
  extends _WatchFilesDialog with JDialogSupport with VirtualWatchFilesDialog {

  import SwingVirtualImplicits._

  override val okButton: VirtualButton = _okButton
  override val closeButton: VirtualButton = _closeButton
  override val watchButton: VirtualButton = _watchButton
  override val deWatchButton: VirtualButton = _deWatchButton
  override val workingTree: VirtualFileTree = _workingTree
  override val watchingList: VirtualList = _watchingList
  override val dialog: VirtualDialog = this

  setSize(Size(600, 400))

}
