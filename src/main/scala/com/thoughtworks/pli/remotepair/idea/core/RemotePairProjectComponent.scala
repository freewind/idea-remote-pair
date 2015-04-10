package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.remotepair.idea.Module

case class RemotePairProjectComponent(currentProject: Project) extends ProjectComponent with Module {

  override def initComponent(): Unit = {
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    logger.info("#################### project opened")
    val connection = createMessageConnection()
    connection.foreach(_.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, myFileEditorManagerFactory.create()))
    connection.foreach(_.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(myVirtualFileAdapterFactory())))
    getStatusBar().addWidget(pairStatusWidgetFactory.create())
    setupProjectStatusListener()
  }

  override def projectClosed(): Unit = {
    logger.info("#################### project closed")
    createMessageConnection().foreach(_.disconnect())
  }

  private def setupProjectStatusListener(): Unit = createMessageConnection().foreach { conn =>
    conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        val am = ActionManager.getInstance()
        val menu = am.getAction("IdeaRemotePair.Menu").asInstanceOf[DefaultActionGroup]
        menu.removeAll()
        menu.add(statusWidgetPopups.createActionGroup())
      }
    })
  }

}
