package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JDialog

import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterWatchingFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.dialogs.BaseVirtualDialog
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.language.reflectiveCalls

class SyncProgressDialog(val currentProject: MyProject, val myIde: MyIde, val pairEventListeners: PairEventListeners)
  extends _SyncProgressDialog with JDialogSupport with BaseVirtualDialog {

  init()

  @volatile private var completed: Int = 0

  override def init(): Unit = {
    monitorReadEvent {
      case MasterWatchingFiles(_, _, _, total) => _progressBar.max = total
      case SyncFileEvent(_, _, path, _) => {
        val total = _progressBar.max
        completed += 1
        _messageLabel.text = s"$path ($completed/$total)"
        _progressBar.value = completed
        //      progressBar.updateUI()
        if (completed == total) {
          _messageLabel.text = _messageLabel.text + " Complete!"
          _closeButton.enabled = true
        }
      }
    }

  }

}
