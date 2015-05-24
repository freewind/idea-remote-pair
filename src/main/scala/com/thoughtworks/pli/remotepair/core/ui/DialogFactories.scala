package com.thoughtworks.pli.remotepair.core.ui

import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog._
import com.thoughtworks.pli.remotepair.core.ui.dialogs._

trait DialogFactories {
  def createConnectServerDialog: VirtualConnectServerDialog
  def createCopyProjectUrlDialog: VirtualCopyProjectUrlDialog
  def createSyncFilesForMasterDialog: VirtualSyncFilesForMasterDialog
  def createSyncFilesForSlaveDialog: VirtualSyncFilesForSlaveDialog
  def createProgressDialog: VirtualSyncProgressDialog
  def createWatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler]): VirtualWatchFilesDialog
}
