package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.idea.idea.GetProjectWindow
import com.thoughtworks.pli.remotepair.core.{PluginLogger, CopyToClipboard}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.settings.ProjectUrlInProjectStorage
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object CopyProjectUrlDialog {
  type Factory = () => CopyProjectUrlDialog
}

class CopyProjectUrlDialog(val invokeLater: InvokeLater, val getProjectWindow: GetProjectWindow, val pairEventListeners: PairEventListeners, copyToClipboard: CopyToClipboard, projectUrlInProjectStorage: ProjectUrlInProjectStorage, logger: PluginLogger)
  extends _CopyProjectUrlDialog with JDialogSupport {

  setTitle("Copy Project Url Dialog")
  setSize(new Size(500, 200))

  val projectUrl = projectUrlInProjectStorage.load()
  projectUrlField.setText(projectUrl.getOrElse(""))

  onClick(copyAndCloseButton) {
    projectUrl match {
      case Some(url) => copyToClipboard(url)
      case _ => logger.error("project url is not found in project storage")
    }
    dispose()
  }

}
