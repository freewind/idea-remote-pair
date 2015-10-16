package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualCopyProjectUrlDialog
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.language.reflectiveCalls

case class CopyProjectUrlDialog(currentProject: MyProject, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
  extends _CopyProjectUrlDialog with JDialogSupport with VirtualCopyProjectUrlDialog {

  val dialog: VirtualDialog = this
  val projectUrlField: VirtualInputField = _projectUrlField
  val copyAndCloseButton: VirtualButton = _copyAndCloseButton

  setSize(new Size(500, 200))
  init()

}
