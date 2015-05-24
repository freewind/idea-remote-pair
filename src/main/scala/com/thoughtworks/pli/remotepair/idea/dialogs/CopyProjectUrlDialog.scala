package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualCopyProjectUrlDialog
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.language.reflectiveCalls

object CopyProjectUrlDialog {
  type Factory = () => CopyProjectUrlDialog
}


case class CopyProjectUrlDialog(currentProject: IdeaProjectImpl, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
  extends _CopyProjectUrlDialog with JDialogSupport with VirtualCopyProjectUrlDialog {

  import SwingVirtualImplicits._

  val dialog: VirtualDialog = this
  val projectUrlField: VirtualInputField = _projectUrlField
  val copyAndCloseButton: VirtualButton = _copyAndCloseButton

  setSize(new Size(500, 200))

}
