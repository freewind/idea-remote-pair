package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.language.reflectiveCalls

object CopyProjectUrlDialog {
  type Factory = () => CopyProjectUrlDialog
}

trait MyCopyProjectUrlDialog extends MyWindow {
  def myProjectStorage: MyProjectStorage
  def mySystem: MySystem
  def logger: PluginLogger

  dialog.title_=("Copy Project Url Dialog")
  val projectUrlField: VirtualInputField
  val copyAndCloseButton: VirtualButton

  private val projectUrl = myProjectStorage.projectUrl
  projectUrlField.text = projectUrl.getOrElse("")

  copyAndCloseButton.onClick {
    projectUrl match {
      case Some(url) => mySystem.copyToClipboard(url)
      case _ => logger.error("project url is not found in project storage")
    }
    dialog.dispose()
  }
}

case class CopyProjectUrlDialog(currentProject: IdeaProjectImpl, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
  extends _CopyProjectUrlDialog with JDialogSupport with MyCopyProjectUrlDialog {

  import SwingVirtualImplicits._

  val dialog: VirtualDialog = this
  val projectUrlField: VirtualInputField = _projectUrlField
  val copyAndCloseButton: VirtualButton = _copyAndCloseButton

  setSize(new Size(500, 200))

}
