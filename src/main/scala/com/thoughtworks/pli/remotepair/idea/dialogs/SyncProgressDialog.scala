package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyIde}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualSyncProgressDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import SwingVirtualImplicits._

import scala.language.reflectiveCalls

class SyncProgressDialog(val currentProject: MyProject, val myIde: MyIde, val pairEventListeners: PairEventListeners)
  extends _SyncProgressDialog with JDialogSupport with VirtualSyncProgressDialog {

  override val dialog: VirtualDialog = this
  override val closeButton: VirtualButton = _closeButton
  override val progressBar: VirtualProgressBar = _progressBar
  override val messageLabel: VirtualLabel = _messageLabel

  init()

}
