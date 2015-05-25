package com.thoughtworks.pli.remotepair.in_memory_ide.dialogs

import com.thoughtworks.pli.remotepair.core.MySystem
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.ui.{DialogFactories, VirtualStatusBar}

class MemoryStatusBar extends VirtualStatusBar {
  override def currentProject: MyProject = ???
  override def myServer: MyServer = ???
  override def mySystem: MySystem = ???
  override def myIde: MyIde = ???
  override def myClient: MyClient = ???
  override def dialogFactories: DialogFactories = ???
  override def myProjectStorage: MyProjectStorage = ???
}
