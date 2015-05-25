package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.NettyClient.Factory
import com.thoughtworks.pli.remotepair.core.client.{MyChannelHandler, MyClient}
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualConnectServerDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.in_memory_ide.MemoryUiComponents.{MemoryButton, MemoryCheckbox, MemoryInputField, MemoryLabel}

class MemoryConnectServerDialog extends VirtualConnectServerDialog {
  override def myUtils: MyUtils = ???
  override def myIde: MyIde = ???
  override def myChannelHandlerFactory: MyChannelHandler.Factory = ???
  override def myClient: MyClient = ???
  override def clientFactory: Factory = ???
  override def dialogFactories: DialogFactories = ???
  override def myProjectStorage = ???
  override val serverPortField = new MemoryInputField
  override val joinProjectButton = new MemoryButton
  override val clientNameInJoinField = new MemoryInputField
  override val readonlyCheckBox = new MemoryCheckbox
  override val serverHostField = new MemoryInputField
  override val messageLabel = new MemoryLabel
  override val projectUrlField = new MemoryInputField
  override val createProjectButton = new MemoryButton
  override val clientNameInCreationField = new MemoryInputField
  override def dialog: VirtualDialog = ???
  override def showOnCenter(): Unit = ???
  override def pairEventListeners: PairEventListeners = ???
}
