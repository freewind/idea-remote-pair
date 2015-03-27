package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, ParseEvent}
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.core.files.{GetFileChildren, GetFileName, IsDirectory}
import com.thoughtworks.pli.remotepair.idea.core.tree.{CreateFileTree, FileTreeNodeDataFactory}
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners.{ProjectCaretListenerFactory, ProjectDocumentListenerFactory, ProjectSelectionListenerFactory}
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidgetFactory
import com.thoughtworks.pli.remotepair.idea.utils._
import org.specs2.mock.Mockito

trait MocksModule {
  this: Mockito =>

  lazy val logger = mock[Logger]

  lazy val newUuid = mock[NewUuid]

  lazy val runtimeAssertions = mock[RuntimeAssertions]
  lazy val md5 = mock[Md5]
  lazy val isSubPath = mock[IsSubPath]
  lazy val localHostName = mock[GetLocalHostName]
  lazy val localIp = mock[GetLocalIp]

  // http://stackoverflow.com/questions/29059045/answers-is-not-invoked-when-mocking-a-method-with-call-by-name-parameter
  lazy val invokeLater = new InvokeLater {
    override def apply(f: => Any): Unit = f
  }
  lazy val runReadAction = new RunReadAction {
    override def apply(f: => Any): Unit = f
  }
  lazy val getIdeaProperties = mock[GetIdeaProperties]
  lazy val serverPortInGlobalStorage = mock[ServerPortInGlobalStorage]
  lazy val clientNameInGlobalStorage = mock[ClientNameInGlobalStorage]
  lazy val currentProject = mock[Project]

  lazy val publishEvent = mock[PublishEvent]
  lazy val runWriteAction = new RunWriteAction(currentProject) {
    override def apply(f: => Any): Unit = f
  }
  lazy val pairEventListeners = mock[PairEventListeners]
  lazy val getCurrentProjectProperties = mock[GetCurrentProjectProperties]
  lazy val serverPortInProjectStorage = mock[ServerPortInProjectStorage]
  lazy val serverHostInProjectStorage = mock[ServerHostInProjectStorage]
  lazy val startServer = mock[StartServer]

  // event handlers
  lazy val publishSyncFilesRequest = mock[PublishSyncFilesRequest]
  lazy val tabEventHandler = mock[TabEventHandler]
  lazy val publishCreateDocumentEvent = mock[PublishCreateDocumentEvent]
  lazy val newHighlights = mock[NewHighlights]
  lazy val removeOldHighlighters = mock[RemoveOldHighlighters]
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = mock[CreateDocumentConfirmation => ClientVersionedDocument]
  lazy val getCachedFileContent = mock[GetCachedFileContent]
  lazy val synchronized = new Synchronized {
    override def apply[T](obj: AnyRef)(f: => T): T = f
  }
  lazy val getFileContent = mock[GetFileContent]
  lazy val getTextEditorsOfPath = mock[GetTextEditorsOfPath]
  lazy val clientVersionedDocuments = mock[ClientVersionedDocuments]
  lazy val writeToProjectFile = mock[WriteToProjectFile]
  lazy val getFileByRelative = mock[GetFileByRelative]
  lazy val highlightNewContent = mock[HighlightNewContent]
  lazy val handleChangeContentConfirmation = mock[HandleChangeContentConfirmation]
  lazy val handleResetTabRequest = mock[HandleResetTabRequest]
  lazy val moveCaret = mock[MoveCaret]
  lazy val handleCreateServerDocumentRequest = mock[HandleCreateServerDocumentRequest]
  lazy val highlightPairSelection = mock[HighlightPairSelection]
  lazy val handleSyncFilesRequest = mock[HandleSyncFilesRequest]
  lazy val handleMasterPairableFiles = mock[HandleMasterWatchingFiles]
  lazy val handleCreateDocumentConfirmation = mock[HandleCreateDocumentConfirmation]
  lazy val handleGetPairableFilesFromPair = mock[HandleGetWatchingFilesFromPair]
  lazy val handleServerStatusResponse = mock[HandleServerStatusResponse]
  lazy val handleJoinedToProjectEvent = mock[HandleJoinedToProjectEvent]
  lazy val handleSyncFilesForAll = mock[HandleSyncFilesForAll]
  lazy val handleSyncFileEvent = mock[HandleSyncFileEvent]
  lazy val findOrCreateDir = mock[FindOrCreateDir]
  lazy val handleCreateDirEvent = mock[HandleCreateDirEvent]
  lazy val deleteProjectFile = mock[DeleteProjectFile]
  lazy val handleDeleteFileEvent = mock[HandleDeleteFileEvent]
  lazy val clientInfoHolder = mock[ClientInfoHolder]
  lazy val handleClientInfoResponse = mock[HandleClientInfoResponse]
  lazy val deleteProjectDir = mock[DeleteProjectDir]
  lazy val handleDeleteDirEvent = mock[HandleDeleteDirEvent]
  lazy val showErrorDialog = mock[ShowServerError]
  lazy val handleCreateFileEvent = mock[HandleCreateFileEvent]
  lazy val handleEvent = mock[HandleEvent]

  lazy val connectionFactory = mock[ConnectionFactory]
  lazy val myChannelHandlerFactory = mock[MyChannelHandlerFactory]

  lazy val chooseIgnoreDialogFactory = mock[WatchFilesDialogFactory]
  lazy val joinProjectDialogFactory = mock[JoinProjectDialogFactory]
  lazy val parseEvent = mock[ParseEvent]
  lazy val clientFactory = mock[ClientFactory]
  lazy val connectServerDialogFactory = mock[ConnectServerDialogFactory]

  lazy val inWatchingList = mock[InWatchingList]
  lazy val getDocumentContent = mock[GetDocumentContent]
  lazy val getUserData = mock[GetUserData]
  lazy val putUserData = mock[PutUserData]
  lazy val getRelativePath = mock[GetRelativePath]
  lazy val getCaretOffset = mock[GetCaretOffset]
  lazy val getSelectionEventInfo = mock[GetSelectionEventInfo]
  lazy val projectCaretListenerFactory = mock[ProjectCaretListenerFactory]
  lazy val projectSelectionListenerFactory = mock[ProjectSelectionListenerFactory]
  lazy val projectDocumentListenerFactory = mock[ProjectDocumentListenerFactory]
  lazy val myFileEditorManagerFactory = mock[MyFileEditorManagerFactory]
  lazy val myVirtualFileAdapterFactory = mock[MyVirtualFileAdapterFactory]
  lazy val clientName = mock[ClientIdToName]
  lazy val syncFilesForSlaveDialogFactory = mock[SyncFilesForSlaveDialogFactory]
  lazy val syncFilesForMasterDialogFactory = mock[SyncFilesForMasterDialogFactory]
  lazy val statusWidgetPopups = mock[StatusWidgetPopups]
  lazy val pairStatusWidgetFactory = mock[PairStatusWidgetFactory]
  lazy val getMyClientId = mock[GetMyClientId]
  lazy val getWatchingFileSummaries = mock[GetWatchingFileSummaries]
  lazy val handleGetWatchingFilesFromPair = mock[HandleGetWatchingFilesFromPair]

  lazy val getFileName = mock[GetFileName]
  lazy val getFileChildren = mock[GetFileChildren]
  lazy val isDirectory = mock[IsDirectory]
  lazy val fileTreeNodeDataFactory = mock[FileTreeNodeDataFactory]
  lazy val createFileTree = mock[CreateFileTree]
}
