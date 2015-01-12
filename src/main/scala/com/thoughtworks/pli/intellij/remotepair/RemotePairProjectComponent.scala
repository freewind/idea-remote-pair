package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.statusbar.{StatusWidgetPopups, PairStatusWidget, DynamicActions}

class RemotePairProjectComponent(project: Project)
  extends ProjectComponent with MyFileEditorManagerAdapter with CurrentProjectHolder with DynamicActions with StatusWidgetPopups with EventHandler {

  override val currentProject = Projects.init(project)

  override def initComponent(): Unit = {
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    log.info("#################### project opened")
    val connection = currentProject.createMessageConnection()
    connection.foreach(_.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileEditorManager()))
    connection.foreach(_.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(new MyVirtualFileAdapter(currentProject))))
    currentProject.getStatusBar.addWidget(new PairStatusWidget(currentProject))
    setupProjectStatusListener()
  }

  override def projectClosed(): Unit = {
    log.info("#################### project closed")
    currentProject.createMessageConnection().foreach(_.disconnect())
  }

  def connect(ip: String, port: Int) = {
    new Client(ip, port).connect(eventHandler = handleEvent,
      onActive = Some(ctx => currentProject.context = Some(ctx)),
      onInactive = Some(_ => currentProject.context = None),
      onError = Some({ case (_, e) => e.printStackTrace()}))
  }

  private def setupProjectStatusListener(): Unit = currentProject.createMessageConnection().foreach { conn =>
    conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        val am = ActionManager.getInstance()
        val menu = am.getAction("IdeaRemotePair.Menu").asInstanceOf[DefaultActionGroup]
        menu.removeAll()
        menu.add(createActionGroup())
      }
    })
  }


}
