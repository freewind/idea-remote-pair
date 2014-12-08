package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.thoughtworks.pli.intellij.remotepair.{PublishSyncFilesRequest, RichProject}

class SendSyncFilesRequestDialog(override val currentProject: RichProject) extends ChooseIgnoreDialog(currentProject) with PublishSyncFilesRequest {

  override def doOKAction(): Unit = {
    super.doOKAction()
    invokeLater {
      try {
        publishSyncFilesRequest(form.ignoredFiles)
      } catch {
        case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
      }
    }
  }

}
