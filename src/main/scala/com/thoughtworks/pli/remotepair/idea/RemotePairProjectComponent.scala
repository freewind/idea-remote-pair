package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.intellij.util.messages.MessageBusConnection
import com.thoughtworks.pli.remotepair.core.ProjectStatusChanges

object RemotePairProjectComponent {
  val moduleKey = new Key[Module]("idea.plugin.module")
}

case class RemotePairProjectComponent(currentIdeaRawProject: Project) extends ProjectComponent {

  val module = new Module(currentIdeaRawProject)

  import module._

  override def initComponent(): Unit = {
    currentIdeaRawProject.putUserData(RemotePairProjectComponent.moduleKey, module)
  }

  override def disposeComponent(): Unit = {
    currentIdeaRawProject.putUserData(RemotePairProjectComponent.moduleKey, null)
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    logger.info("project opened")
    currentProject.createMessageConnection() match {
      case Some(connection) =>
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, myFileEditorManagerFactory())
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(myVirtualFileAdapterFactory()))
        currentProject.statusBar.addWidget(ideaStatusWidgetFactory())
        setupProjectStatusListener(connection)
      case _ =>
    }
  }

  override def projectClosed(): Unit = {
    logger.info("project closed")
    currentProject.createMessageConnection().foreach(_.disconnect())
  }

  private def setupProjectStatusListener(connection: MessageBusConnection): Unit = {
    connection.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        val am = ActionManager.getInstance()
        val menu = am.getAction("IdeaRemotePair.Menu").asInstanceOf[DefaultActionGroup]
        menu.removeAll()
        menu.add(ideaStatusWidgetFactory().createActionGroup())
      }
    })
  }

}
