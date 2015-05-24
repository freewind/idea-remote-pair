package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterWatchingFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualLabel, VirtualProgressBar}

trait VirtualSyncProgressDialog extends MonitorEvents {
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
