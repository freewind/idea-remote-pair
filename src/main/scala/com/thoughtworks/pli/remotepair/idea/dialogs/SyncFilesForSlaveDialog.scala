package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualSyncFilesForSlaveDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.language.reflectiveCalls

object SyncFilesForSlaveDialog {
  type Factory = () => SyncFilesForSlaveDialog
}

case class SyncFilesForSlaveDialog(currentProject: IdeaProjectImpl, myClient: MyClient, watchFilesDialogFactory: WatchFilesDialog.Factory, myIde: MyIde, pairEventListeners: PairEventListeners)
  extends _SyncFilesBaseDialog with JDialogSupport with VirtualSyncFilesForSlaveDialog {

  import SwingVirtualImplicits._

  override val dialog: VirtualDialog = this
  override val okButton: VirtualButton = _okButton
  override val cancelButton: VirtualButton = _cancelButton
  override val configButton: VirtualButton = _configButton
  override val tabs = _tabs

}

