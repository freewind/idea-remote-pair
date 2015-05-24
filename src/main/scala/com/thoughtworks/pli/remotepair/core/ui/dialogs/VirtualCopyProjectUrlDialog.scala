package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualInputField}
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}

trait VirtualCopyProjectUrlDialog extends BaseVirtualDialog {
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
