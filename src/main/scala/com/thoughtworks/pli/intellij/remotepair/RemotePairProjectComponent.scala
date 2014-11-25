package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.statusbar.PairStatusWidget

class RemotePairProjectComponent(project: Project) extends ProjectComponent
with Subscriber with MyFileEditorManagerAdapter with CurrentProjectHolder {

  override val currentProject = Projects.init(project)

  override def initComponent(): Unit = {
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    val connection = currentProject.getMessageBus.connect(currentProject.raw)
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileEditorManager())
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(MyVirtualFileAdapter))
    currentProject.getStatusBar.addWidget(new PairStatusWidget(currentProject))
  }

  override def projectClosed() {
  }

  def connect(ip: String, port: Int) = {
    subscribe(ip, port)
  }

}
