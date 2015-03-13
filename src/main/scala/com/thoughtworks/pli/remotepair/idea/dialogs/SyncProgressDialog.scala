package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterPairableFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class SyncProgressDialog(val currentProject: RichProject, val invokeLater: InvokeLater,val pairEventListeners: PairEventListeners)
  extends _SyncProgressDialog with JDialogSupport {

  @volatile private var completed: Int = 0

  monitorReadEvent {
    case MasterPairableFiles(_, _, _, total) => progressBar.getModel.setMaximum(total)
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
