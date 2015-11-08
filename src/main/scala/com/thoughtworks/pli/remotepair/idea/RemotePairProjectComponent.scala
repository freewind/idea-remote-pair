package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.intellij.util.messages.MessageBusConnection
import com.thoughtworks.pli.remotepair.core.ProjectStatusChanges

class RemotePairProjectComponent(val currentIdeaRawProject: Project) extends ProjectComponent with Module {

  override def initComponent(): Unit = ()

  override def disposeComponent(): Unit = ()

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened(): Unit = {
    logger.info("project opened")
    currentProject.createMessageConnection() match {
      case Some(connection) =>
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, myFileEditorManagerFactory())
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(myVirtualFileAdapterFactory()))
        currentProject.statusBar.addWidget(ideaStatusWidgetFactory())
        setupProjectStatusListener(connection)
      case _ => logger.error("Can't get message connection when project is opened")
    }
  }

  override def projectClosed(): Unit = {
    logger.info("project closed")
    currentProject.createMessageConnection().foreach(_.disconnect())
  }

  private def setupProjectStatusListener(connection: MessageBusConnection): Unit = {
    connection.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        recreateActionMenu()
        clearClientVersionedDocuments()
        clearPairSelections()
        clearPairCaret()
      }

      private def recreateActionMenu(): Unit = {
        val menu = ActionManager.getInstance().getAction("IdeaRemotePair.Menu").asInstanceOf[DefaultActionGroup]
        menu.removeAll()
        menu.add(ideaStatusWidgetFactory().createActionGroup())
      }
      private def clearClientVersionedDocuments(): Unit = if (!myClient.isConnected) clientVersionedDocuments.clear()
      private def clearPairSelections(): Unit = pairSelections.clearAll()
      private def clearPairCaret(): Unit = {
        pairCarets.clearAll()
      }
    })
  }

}
