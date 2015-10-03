package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.{SyncFileEvent, MasterWatchingFiles}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualLabel, VirtualProgressBar}

trait VirtualSyncProgressDialog extends BaseVirtualDialog {
  @volatile private var completed: Int = 0

  val closeButton: VirtualButton
  val progressBar: VirtualProgressBar
  val messageLabel: VirtualLabel

  override def init(): Unit = {
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
}