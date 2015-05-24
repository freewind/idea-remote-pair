package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualConnectServerDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.language.reflectiveCalls

object ConnectServerDialog {
  type Factory = () => ConnectServerDialog
}

case class ConnectServerDialog(currentProject: IdeaProjectImpl, myProjectStorage: MyProjectStorage, myIde: MyIde, myUtils: MyUtils, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, watchFilesDialogFactory: WatchFilesDialog.Factory, copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory, myClient: MyClient)
  extends _ConnectServerDialog with JDialogSupport with VirtualConnectServerDialog {

  import SwingVirtualImplicits._

  override val serverHostField: VirtualInputField = _hostTextField
  override val serverPortField: VirtualInputField = _portTextField
  override val projectUrlField: VirtualInputField = _joinUrlField
  override val clientNameField: VirtualInputField = _clientNameInCreationField
  override val messageLabel: VirtualLabel = _message
  override val dialog: VirtualDialog = this
  override val readonlyCheckBox: VirtualCheckBox = _readonlyCheckBox
  override val createProjectButton: VirtualButton = _createProjectButton
  override val joinProjectButton: VirtualButton = _joinProjectButton

  setSize(Size(400, 220))

}





