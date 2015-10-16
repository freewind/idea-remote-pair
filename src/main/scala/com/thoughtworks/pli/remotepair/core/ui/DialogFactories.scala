package com.thoughtworks.pli.remotepair.core.ui

import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.dialogs._

trait DialogFactories {
  def createConnectServerDialog: ConnectServerDialog
  def createCopyProjectUrlDialog: CopyProjectUrlDialog
  def createSyncFilesForMasterDialog: SyncFilesForMasterDialog
  def createSyncFilesForSlaveDialog: SyncFilesForSlaveDialog
  def createProgressDialog: SyncProgressDialog
  def createWatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler]): WatchFilesDialog
}
