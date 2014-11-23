package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.intellij.util.messages.MessageBusConnection
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class RemotePairProjectComponent(project: Project) extends ProjectComponent
with Subscriber with MyFileEditorManagerAdapter with CurrentProjectHolder {

  override val currentProject = new RichProject(project)

  override def initComponent() {
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    val project = currentProject.raw
    val connection: MessageBusConnection = project.getMessageBus.connect(project)
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileEditorManager())
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(MyVirtualFileAdapter))
  }

  override def projectClosed() {
  }

  def connect(ip: String, port: Int) = {
    subscribe(ip, port)
  }

}
