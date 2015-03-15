package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.softwaremill.macwire.Macwire
import com.softwaremill.macwire.scopes.{ProxyingScope, Scope}
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners.{ProjectCaretListener, ProjectSelectionListener, ProjectDocumentListener}
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.{PairStatusWidgetFactory, StatusWidgetPopups}
import com.thoughtworks.pli.remotepair.idea.utils._

import scala.collection.mutable
import scala.reflect.ClassTag

trait UtilsModule extends Macwire {
  lazy val logger = Logger.getInstance(this.getClass)

  lazy val newUuid = wire[NewUuid]

  lazy val runtimeAssertions = wire[RuntimeAssertions]
  lazy val md5 = wire[Md5]
  lazy val isSubPath = wire[IsSubPath]
  lazy val localHostName = wire[GetLocalHostName]
  lazy val localIp = wire[GetLocalIp]
  lazy val invokeLater = wire[InvokeLater]
  lazy val runReadAction = wire[RunReadAction]
  lazy val richProjectFactory = wire[RichProjectFactory]
  lazy val getIdeaProperties = wire[GetIdeaProperties]
  lazy val ideaSettingsProperties = wire[IdeaSettingsProperties]
}

case class ProjectScope(rawProject: Project) extends Scope {

  override def apply[T](createT: => T)(implicit tag: ClassTag[T]): T = {
    val keyName = "idea-remote-pair." + tag.runtimeClass.getName
    println("########## keyName: " + keyName)
    get[T](keyName, createT)
  }

  override def get[T](keyName: String, createT: => T): T = {
    val key = Keys[T](keyName)
    rawProject.getUserData[T](key) match {
      case null => {
        val newValue = createT
        rawProject.putUserData(key, newValue)
        newValue
      }
      case value => value
    }
  }
}

object Keys {
  private val map = new mutable.HashMap[String, Key[_]]()
  def apply[T](name: String): Key[T] = map.synchronized {
    map.get(name) match {
      case Some(key) => key.asInstanceOf[Key[T]]
      case _ => val key = new Key[T](name)
        map.put(name, key)
        key
    }
  }
}

trait CurrentProjectModule extends Macwire with UtilsModule {
  def rawProject: Project
  lazy val projectScope: Scope = new ProjectScope(rawProject)
  lazy val currentProject: RichProject = projectScope(richProjectFactory.create(rawProject))
}

trait Module extends Macwire with CurrentProjectModule {

  lazy val publishEvent = projectScope(wire[PublishEvent])
  lazy val runWriteAction = projectScope(wire[RunWriteAction])
  lazy val pairEventListeners = projectScope(wire[PairEventListeners])

  lazy val getCurrentProjectProperties = projectScope(wire[GetCurrentProjectProperties])
  lazy val projectSettingsProperties = projectScope(wire[ProjectSettingsProperties])
  lazy val startServer = projectScope(wire[StartServer])

  // event handlers
  lazy val publishSyncFilesRequest = projectScope(wire[PublishSyncFilesRequest])
  lazy val tabEventHandler = projectScope(wire[TabEventHandler])
  lazy val publishCreateDocumentEvent = projectScope(wire[PublishCreateDocumentEvent])
  lazy val newHighlights = projectScope(wire[NewHighlights])
  lazy val removeOldHighlighters = projectScope(wire[RemoveOldHighlighters])
  lazy val clientVersionedDocumentFactory = projectScope(wire[ClientVersionedDocumentFactory])
  lazy val clientVersionedDocuments = projectScope(wire[ClientVersionedDocuments])
  lazy val handleChangeContentConfirmation = projectScope(wire[HandleChangeContentConfirmation])
  lazy val handleResetTabRequest = projectScope(wire[HandleResetTabRequest])
  lazy val moveCaret = projectScope(wire[MoveCaret])
  lazy val handleCreateServerDocumentRequest = projectScope(wire[HandleCreateServerDocumentRequest])
  lazy val highlightPairSelection = projectScope(wire[HighlightPairSelection])
  lazy val handleSyncFilesRequest = projectScope(wire[HandleSyncFilesRequest])
  lazy val handleMasterPairableFiles = projectScope(wire[HandleMasterPairableFiles])
  lazy val handleCreateDocumentConfirmation = projectScope(wire[HandleCreateDocumentConfirmation])
  lazy val handleGetPairableFilesFromPair = projectScope(wire[HandleGetPairableFilesFromPair])
  lazy val handleServerStatusResponse = projectScope(wire[HandleServerStatusResponse])
  lazy val handleJoinedToProjectEvent = projectScope(wire[HandleJoinedToProjectEvent])
  lazy val handleSyncFilesForAll = projectScope(wire[HandleSyncFilesForAll])
  lazy val handleSyncFileEvent = projectScope(wire[HandleSyncFileEvent])
  lazy val handleCreateDirEvent = projectScope(wire[HandleCreateDirEvent])
  lazy val handleDeleteFileEvent = projectScope(wire[HandleDeleteFileEvent])
  lazy val handleClientInfoResponse = projectScope(wire[HandleClientInfoResponse])
  lazy val handleDeleteDirEvent = projectScope(wire[HandleDeleteDirEvent])
  lazy val showErrorDialog = projectScope(wire[ShowServerError])
  lazy val handleCreateFileEvent = projectScope(wire[HandleCreateFileEvent])
  lazy val handleEvent = projectScope(wire[HandleEvent])

  lazy val connectionFactory = projectScope(wire[ConnectionFactory])
  lazy val myChannelHandlerFactory = projectScope(wire[MyChannelHandlerFactory])

  lazy val chooseIgnoreDialogFactory = projectScope(wire[ChooseIgnoreDialogFactory])
  lazy val joinProjectDialogFactory = projectScope(wire[JoinProjectDialogFactory])
  lazy val parseEvent = projectScope(wire[ParseEvent])
  lazy val clientFactory = projectScope(wire[ClientFactory])
  lazy val connectServerDialogFactory = projectScope(wire[ConnectServerDialogFactory])

  lazy val projectCaretListener = projectScope(wire[ProjectCaretListener])
  lazy val projectSelectionListener = projectScope(wire[ProjectSelectionListener])
  lazy val projectDocumentListener = projectScope(wire[ProjectDocumentListener])
  lazy val myFileEditorManagerFactory = projectScope(wire[MyFileEditorManagerFactory])
  lazy val myVirtualFileAdapterFactory = projectScope(wire[MyVirtualFileAdapterFactory])
  lazy val clientName = projectScope(wire[ClientName])
  lazy val syncFilesForSlaveDialogFactory = projectScope(wire[SyncFilesForSlaveDialogFactory])
  lazy val syncFilesForMasterDialogFactory = projectScope(wire[SyncFilesForMasterDialogFactory])
  lazy val statusWidgetPopups = projectScope(wire[StatusWidgetPopups])
  lazy val pairStatusWidgetFactory = projectScope(wire[PairStatusWidgetFactory])

}
