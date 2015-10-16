package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JDialog

import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.dialogs.BaseVirtualDialog
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.language.reflectiveCalls

case class CopyProjectUrlDialog(currentProject: MyProject, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
  extends _CopyProjectUrlDialog with JDialogSupport with BaseVirtualDialog {

  def init(): Unit = {
    this.title = "Copy Project Url Dialog"

    val projectUrl = myProjectStorage.projectUrl
    _projectUrlField.text = projectUrl.getOrElse("")

    _copyAndCloseButton.onClick {
      projectUrl match {
        case Some(url) => mySystem.copyToClipboard(url)
        case _ => logger.error("project url is not found in project storage")
      }
      this.dispose()
    }
  }


  setSize(new Size(500, 200))
  init()

}
