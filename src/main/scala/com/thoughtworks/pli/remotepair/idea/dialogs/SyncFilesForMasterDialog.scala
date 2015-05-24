package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualSyncFilesForMasterDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl
import SwingVirtualImplicits._

import scala.language.reflectiveCalls

case class SyncFilesForMasterDialog(currentProject: IdeaProjectImpl, myIde: MyIde, myClient: MyClient, dialogFactories: DialogFactories, pairEventListeners: PairEventListeners)
  extends _SyncFilesBaseDialog with JDialogSupport with VirtualSyncFilesForMasterDialog {


  val dialog: VirtualDialog = this
  val okButton: VirtualButton = _okButton
  val cancelButton: VirtualButton = _cancelButton
  val configButton: VirtualButton = _configButton
  val tabs = _tabs

  init()
}
