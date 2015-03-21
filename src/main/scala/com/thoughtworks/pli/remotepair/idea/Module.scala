package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.softwaremill.macwire.Macwire
import com.softwaremill.macwire.scopes.Scope
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.core.files.{IsDirectory, GetFileChildren, GetFileName}
import com.thoughtworks.pli.remotepair.idea.core.tree.{CreateFileTree, FileTreeNodeDataFactory}
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidgetFactory
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
  lazy val getIdeaProperties = wire[GetIdeaProperties]
  lazy val clientNameInGlobalStorage = wire[ClientNameInGlobalStorage]
  lazy val serverPortInGlobalStorage = wire[ServerPortInGlobalStorage]
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
  def currentProject: Project
  lazy val projectScope: Scope = new ProjectScope(currentProject)
}

trait Module extends Macwire with CurrentProjectModule {

  lazy val getMessageBus = projectScope(wire[GetMessageBus])
  lazy val notifyChanges = projectScope(wire[NotifyChanges])
  lazy val connectionHolder = projectScope(wire[ConnectionHolder])
  lazy val publishEvent = projectScope(wire[PublishEvent])
  lazy val runWriteAction = projectScope(wire[RunWriteAction])
  lazy val pairEventListeners = projectScope(wire[PairEventListeners])

  lazy val getCurrentProjectProperties = projectScope(wire[GetCurrentProjectProperties])
  lazy val targetProjectNameInProjectStorage = projectScope(wire[ProjectNameInProjectStorage])
  lazy val targetServerPortInProjectStorage = projectScope(wire[ServerPortInProjectStorage])
  lazy val targetServerHostInProjectStorage = projectScope(wire[ServerHostInProjectStorage])

  lazy val showErrorDialog = projectScope(wire[ShowErrorDialog])
  lazy val showMessageDialog = projectScope(wire[ShowMessageDialog])
  lazy val serverHolder = projectScope(wire[ServerHolder])
  lazy val startServer = projectScope(wire[StartServer])

  // event handlers
  lazy val clientInfoHolder = projectScope(wire[ClientInfoHolder])
  lazy val getMyClientId = projectScope(wire[GetMyClientId])
  lazy val serverStatusHolder = projectScope(wire[ServerStatusHolder])
  lazy val getProjectInfoData = projectScope(wire[GetProjectInfoData])
  lazy val getServerWatchingFiles = projectScope(wire[GetServerWatchingFiles])
  lazy val getProjectBaseDir = projectScope(wire[GetProjectBaseDir])
  lazy val standardizePath = projectScope(wire[StandardizePath])
  lazy val getProjectBasePath = projectScope(wire[GetProjectBasePath])
  lazy val getRelativePath = projectScope(wire[GetRelativePath])
  lazy val getFileContent = projectScope(wire[GetFileContent])
  lazy val getFileSummary = projectScope(wire[GetFileSummary])
  lazy val getFileName = projectScope(wire[GetFileName])
  lazy val fileTreeNodeDataFactory = projectScope(wire[FileTreeNodeDataFactory])
  lazy val getFileChildren = projectScope(wire[GetFileChildren])
  lazy val isDirectory = projectScope(wire[IsDirectory])
  lazy val createFileTree = projectScope(wire[CreateFileTree])
  lazy val isWatching = projectScope(wire[IsWatching])
  lazy val getAllWatchingFiles = projectScope(wire[GetAllWatchingFiles])
  lazy val getWatchingFileSummaries = projectScope(wire[GetWatchingFileSummaries])
  lazy val publishSyncFilesRequest = projectScope(wire[PublishSyncFilesRequest])
  lazy val getFileEditorManager = projectScope(wire[GetFileEditorManager])
  lazy val closeFile = projectScope(wire[CloseFile])
  lazy val getOpenFileDescriptor = projectScope(wire[GetOpenFileDescriptor])
  lazy val getFileByRelative = projectScope(wire[GetFileByRelative])
  lazy val tabEventHandler = projectScope(wire[TabEventHandler])
  lazy val publishCreateDocumentEvent = projectScope(wire[PublishCreateDocumentEvent])
  lazy val newHighlights = projectScope(wire[NewHighlights])
  lazy val removeOldHighlighters = projectScope(wire[RemoveOldHighlighters])
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = projectScope(_ => wire[ClientVersionedDocument])
  lazy val clientVersionedDocuments = projectScope(wire[ClientVersionedDocuments])
  lazy val getDocumentContent = projectScope(wire[GetDocumentContent])
  lazy val getCachedFileContent = projectScope(wire[GetCachedFileContent])
  lazy val getEditorsOfPath = projectScope(wire[GetEditorsOfPath])
  lazy val getTextEditorsOfPath = projectScope(wire[GetTextEditorsOfPath])
  lazy val findOrCreateDir = projectScope(wire[FindOrCreateDir])
  lazy val findOrCreateFile = projectScope(wire[FindOrCreateFile])
  lazy val writeToProjectFile = projectScope(wire[WriteToProjectFile])
  lazy val highlightNewContent = projectScope(wire[HighlightNewContent])
  lazy val synchronized = projectScope(wire[Synchronized])
  lazy val handleChangeContentConfirmation = projectScope(wire[HandleChangeContentConfirmation])
  lazy val getFileOfEditor = projectScope(wire[GetFileOfEditor])
  lazy val getEditorPath = projectScope(wire[GetEditorPath])
  lazy val getSelectedTextEditor = projectScope(wire[GetSelectedTextEditor])
  lazy val handleResetTabRequest = projectScope(wire[HandleResetTabRequest])
  lazy val isCaretSharing = projectScope(wire[IsCaretSharing])
  lazy val moveCaret = projectScope(wire[MoveCaret])
  lazy val handleCreateServerDocumentRequest = projectScope(wire[HandleCreateServerDocumentRequest])
  lazy val highlightPairSelection = projectScope(wire[HighlightPairSelection])
  lazy val handleSyncFilesRequest = projectScope(wire[HandleSyncFilesRequest])
  lazy val handleMasterPairableFiles = projectScope(wire[HandleMasterWatchingFiles])
  lazy val handleCreateDocumentConfirmation = projectScope(wire[HandleCreateDocumentConfirmation])
  lazy val handleGetPairableFilesFromPair = projectScope(wire[HandleGetWatchingFilesFromPair])
  lazy val getOpenedFiles = projectScope(wire[GetOpenedFiles])
  lazy val handleServerStatusResponse = projectScope(wire[HandleServerStatusResponse])
  lazy val handleJoinedToProjectEvent = projectScope(wire[HandleJoinedToProjectEvent])
  lazy val handleSyncFilesForAll = projectScope(wire[HandleSyncFilesForAll])
  lazy val handleSyncFileEvent = projectScope(wire[HandleSyncFileEvent])
  lazy val deleteProjectFile = projectScope(wire[DeleteProjectFile])
  lazy val handleCreateDirEvent = projectScope(wire[HandleCreateDirEvent])
  lazy val handleDeleteFileEvent = projectScope(wire[HandleDeleteFileEvent])
  lazy val deleteProjectDir = projectScope(wire[DeleteProjectDir])
  lazy val handleClientInfoResponse = projectScope(wire[HandleClientInfoResponse])
  lazy val handleDeleteDirEvent = projectScope(wire[HandleDeleteDirEvent])
  lazy val handleCreateFileEvent = projectScope(wire[HandleCreateFileEvent])
  lazy val showServerError = projectScope(wire[ShowServerError])
  lazy val handleEvent = projectScope(wire[HandleEvent])

  lazy val connectionFactory = projectScope(wire[ConnectionFactory])
  lazy val myChannelHandlerFactory = projectScope(wire[MyChannelHandlerFactory])


  lazy val removeSelectedItemsFromList = projectScope(wire[RemoveSelectedItemsFromList])
  lazy val getListItems = projectScope(wire[GetListItems])
  lazy val getSelectedFromFileTree = projectScope(wire[GetSelectedFromFileTree])
  lazy val initListItems = projectScope(wire[InitListItems])
  lazy val resetTreeWithExpandedPathKept = projectScope(wire[ResetTreeWithExpandedPathKept])
  lazy val initFileTree = projectScope(wire[InitFileTree])
  lazy val getExistingProjects = projectScope(wire[GetExistingProjects])
  lazy val removeDuplicatePaths = projectScope(wire[RemoveDuplicatePaths])
  lazy val getProjectWindow = projectScope(wire[GetProjectWindow])
  lazy val watchFilesDialogFactory = projectScope(wire[WatchFilesDialogFactory])
  lazy val joinProjectDialogFactory = projectScope(wire[JoinProjectDialogFactory])
  lazy val parseEvent = projectScope(wire[ParseEvent])
  lazy val clientFactory = projectScope(wire[ClientFactory])
  lazy val channelHandlerHolder = projectScope(wire[ChannelHandlerHolder])
  lazy val connectServerDialogFactory = projectScope(wire[ConnectServerDialogFactory])

  lazy val inWatchingList = projectScope(wire[InWatchingList])
  lazy val getUserData = projectScope(wire[GetUserData])
  lazy val putUserData = projectScope(wire[PutUserData])
  lazy val getCaretOffset = projectScope(wire[GetCaretOffset])
  lazy val projectCaretListenerFactory = projectScope(wire[ProjectCaretListenerFactory])
  lazy val getSelectionEventInfo = projectScope(wire[GetSelectionEventInfo])
  lazy val projectSelectionListenerFactory = projectScope(wire[ProjectSelectionListenerFactory])
  lazy val projectDocumentListenerFactory = projectScope(wire[ProjectDocumentListenerFactory])
  lazy val myFileEditorManagerFactory = projectScope(wire[MyFileEditorManagerFactory])
  lazy val containsProjectFile = projectScope(wire[ContainsProjectFile])
  lazy val myVirtualFileAdapterFactory = projectScope(wire[MyVirtualFileAdapterFactory])
  lazy val clientName = projectScope(wire[ClientName])
  lazy val getAllClients = projectScope(wire[GetAllClients])
  lazy val getMasterClientId = projectScope(wire[GetMasterClientId])
  lazy val syncFilesForSlaveDialogFactory = projectScope(wire[SyncFilesForSlaveDialogFactory])
  lazy val getOtherClients = projectScope(wire[GetOtherClients])
  lazy val syncFilesForMasterDialogFactory = projectScope(wire[SyncFilesForMasterDialogFactory])
  lazy val amIMaster = projectScope(wire[AmIMaster])
  lazy val closeConnection = projectScope(wire[CloseConnection])
  lazy val statusWidgetPopups = projectScope(wire[StatusWidgetPopups])
  lazy val createMessageConnection = projectScope(wire[CreateMessageConnection])
  lazy val pairStatusWidgetFactory = projectScope(wire[PairStatusWidgetFactory])
  lazy val getStatusBar = projectScope(wire[GetStatusBar])

}
