package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterWatchingFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class SyncProgressDialog(val currentProject: IdeaProjectImpl, val myIde: MyIde, val pairEventListeners: PairEventListeners)
  extends _SyncProgressDialog with JDialogSupport {

  @volatile private var completed: Int = 0

  monitorReadEvent {
    case MasterWatchingFiles(_, _, _, total) => progressBar.getModel.setMaximum(total)
    case SyncFileEvent(_, _, path, _) => {
      val total = progressBar.getModel.getMaximum
      completed += 1
      messageLabel.setText(s"$path ($completed/$total)")
      progressBar.getModel.setValue(completed)
      progressBar.updateUI()

      if (completed == total) {
        messageLabel.setText(messageLabel.getText + " Complete!")
        closeButton.setEnabled(true)
      }
    }
  }

}
