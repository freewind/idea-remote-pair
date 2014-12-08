package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.remotepair.{RichProject, SyncFilesForAll}

class SendSyncFilesForAllDialog(override val currentProject: RichProject) extends ChooseIgnoreDialog(currentProject) {

  setFormTitle("Choose the files you don't want to synchronize to others")

  override def doOKAction(): Unit = {
    super.doOKAction()
    invokeLater {
      try {
        publishEvent(SyncFilesForAll)
      } catch {
        case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
      }
    }
  }

}
