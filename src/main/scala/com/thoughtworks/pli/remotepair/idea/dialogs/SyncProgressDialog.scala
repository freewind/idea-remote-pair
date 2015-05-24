package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._

import language.reflectiveCalls
import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterWatchingFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

trait MySyncProgressDialog extends MyWindow {
  @volatile private var completed: Int = 0

  val closeButton: VirtualButton
  val progressBar: VirtualProgressBar
  val messageLabel: VirtualLabel

  monitorReadEvent {
    case MasterWatchingFiles(_, _, _, total) => progressBar.max = total
    case SyncFileEvent(_, _, path, _) => {
      val total = progressBar.max
      completed += 1
      messageLabel.text = s"$path ($completed/$total)"
      progressBar.value = completed
      //      progressBar.updateUI()
      if (completed == total) {
        messageLabel.text = messageLabel.text + " Complete!"
        closeButton.enabled = true
      }
    }
  }
}

class SyncProgressDialog(val currentProject: IdeaProjectImpl, val myIde: MyIde, val pairEventListeners: PairEventListeners)
  extends _SyncProgressDialog with JDialogSupport with MySyncProgressDialog {

  import SwingVirtualImplicits._

  override val dialog: VirtualDialog = this
  override val closeButton: VirtualButton = _closeButton
  override val progressBar: VirtualProgressBar = _progressBar
  override val messageLabel: VirtualLabel = _messageLabel
}
