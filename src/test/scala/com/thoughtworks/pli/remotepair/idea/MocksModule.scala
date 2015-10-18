//package com.thoughtworks.pli.remotepair.idea
//
//import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, ParseEvent}
//import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
//import com.thoughtworks.pli.remotepair.core._
//import com.thoughtworks.pli.remotepair.core.client._
//import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
//import com.thoughtworks.pli.remotepair.core.models.MyProject
//import com.thoughtworks.pli.remotepair.core.server_event_handlers._
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.files.{HandleCreateDirEvent, HandleCreateFileEvent, HandleDeleteDirEvent, HandleDeleteFileEvent}
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleJoinedToProjectEvent, HandleServerStatusResponse}
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest, PublishSyncFilesRequest}
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles}
//import com.thoughtworks.pli.remotepair.idea.dialogs._
//import com.thoughtworks.pli.remotepair.idea.listeners._
//
//import org.specs2.mock.Mockito
//
//trait MocksModule {
//  this: Mockito =>
//
//  lazy val pluginLogger = mock[PluginLogger]
//
//  lazy val newUuid = mock[NewUuid]
//
//  lazy val runtimeAssertions = mock[RuntimeAssertions]
//  lazy val md5 = mock[Md5]
//  lazy val isSubPath = mock[IsSubPath]
//  lazy val localHostName = mock[GetLocalHostName]
//  lazy val localIp = mock[GetLocalIp]
//
//  lazy val getIdeaProperties = mock[GetIdeaProperties]
//  lazy val serverPortInGlobalStorage = mock[ServerPortInGlobalStorage]
//  lazy val clientNameInGlobalStorage = mock[ClientNameInGlobalStorage]
//  lazy val currentProject = mock[MyProject]
//
//  // http://stackoverflow.com/questions/29059045/answers-is-not-invoked-when-mocking-a-method-with-call-by-name-parameter
//  lazy val myIde = new myIde {
//    override def invokeLater(f: => Any): Unit = f
//    override def runWriteAction(f: => Any): Unit = f
//    override def runReadAction(f: => Any): Unit = f
//    override def showErrorDialog(title: String, message: String): Unit = ()
//  }
//
//  lazy val pairEventListeners = mock[PairEventListeners]
//  lazy val getCurrentProjectProperties = mock[GetCurrentProjectProperties]
//  lazy val serverPortInProjectStorage = mock[ServerPortInProjectStorage]
//  lazy val serverHostInProjectStorage = mock[ServerHostInProjectStorage]
//  lazy val startServer = mock[StartServer]
//
//  // event handlers
//  lazy val publishSyncFilesRequest = mock[PublishSyncFilesRequest]
//  lazy val tabEventsLocksInProject = mock[TabEventsLocksInProject]
//  lazy val getCurrentTimeMillis = mock[GetCurrentTimeMillis]
//  lazy val handleOpenTabEvent = mock[HandleOpenTabEvent]
//  lazy val handleCloseTabEvent = mock[HandleCloseTabEvent]
//  lazy val publishCreateDocumentEvent = mock[PublishCreateDocumentEvent]
//  lazy val newHighlights = mock[NewHighlights]
//  lazy val removeOldHighlighters = mock[RemoveOldHighlighters]
//  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = mock[CreateDocumentConfirmation => ClientVersionedDocument]
//  lazy val isInPathList = mock[IsInPathList]
//  lazy val synchronized = new Synchronized {
//    override def apply[T](obj: AnyRef)(f: => T): T = f
//  }
//  lazy val getTextEditorsOfPath = mock[GetTextEditorsOfPath]
//  lazy val clientVersionedDocuments = mock[ClientVersionedDocuments]
//  lazy val writeToProjectFile = mock[WriteToProjectFile]
//  lazy val highlightNewContent = mock[HighlightNewContent]
//  lazy val handleChangeContentConfirmation = mock[HandleChangeContentConfirmation]
//  lazy val moveCaret = mock[HandleMoveCaretEvent]
//  lazy val handleCreateServerDocumentRequest = mock[HandleCreateServerDocumentRequest]
//  lazy val getDocumentLength = mock[GetDocumentLength]
//  lazy val highlightPairSelection = mock[HighlightPairSelection]
//  lazy val handleSyncFilesRequest = mock[HandleSyncFilesRequest]
//  lazy val handleMasterPairableFiles = mock[HandleMasterWatchingFiles]
//  lazy val handleCreateDocumentConfirmation = mock[HandleCreateDocumentConfirmation]
//  lazy val handleGetPairableFilesFromPair = mock[HandleGetWatchingFilesFromPair]
//  lazy val handleServerStatusResponse = mock[HandleServerStatusResponse]
//  lazy val handleJoinedToProjectEvent = mock[HandleJoinedToProjectEvent]
//  lazy val handleSyncFilesForAll = mock[HandleSyncFilesForAll]
//  lazy val handleSyncFileEvent = mock[HandleSyncFileEvent]
//  lazy val handleCreateDirEvent = mock[HandleCreateDirEvent]
//  lazy val handleDeleteFileEvent = mock[HandleDeleteFileEvent]
//  lazy val handleClientInfoResponse = mock[HandleClientInfoResponse]
//  lazy val handleDeleteDirEvent = mock[HandleDeleteDirEvent]
//  lazy val showErrorDialog = mock[ShowServerError]
//  lazy val handleCreateFileEvent = mock[HandleCreateFileEvent]
//  lazy val handleEvent = mock[HandleEvent]
//
//  lazy val connectionFactory = mock[Connection.Factory]
//  lazy val myChannelHandlerFactory = mock[MyChannelHandler.Factory]
//
//  lazy val chooseIgnoreDialogFactory = mock[WatchFilesDialog.Factory]
//  lazy val parseEvent = mock[ParseEvent]
//  lazy val clientFactory = mock[Client.Factory]
//  lazy val connectServerDialogFactory = mock[ConnectServerDialog.Factory]
//
//  lazy val getDocumentContent = mock[GetDocumentContent]
//  lazy val getCaretOffset = mock[GetCaretOffset]
//  lazy val getSelectionEventInfo = mock[GetSelectionEventInfo]
//  lazy val projectCaretListenerFactory = mock[ProjectCaretListenerFactory]
//  lazy val projectSelectionListenerFactory = mock[ProjectSelectionListenerFactory]
//  lazy val projectDocumentListenerFactory = mock[ProjectDocumentListenerFactory]
//  lazy val myFileEditorManagerFactory = mock[MyFileEditorManager.Factory]
//  lazy val myVirtualFileAdapterFactory = () => mock[MyVirtualFileAdapter]
//  lazy val syncFilesForSlaveDialogFactory = mock[SyncFilesForSlaveDialog.Factory]
//  lazy val syncFilesForMasterDialogFactory = mock[SyncFilesForMasterDialog.Factory]
//  lazy val statusWidgetPopups = mock[StatusWidgetPopups]
//  lazy val pairStatusWidgetFactory = mock[PairStatusWidget.Factory]
//  lazy val handleGetWatchingFilesFromPair = mock[HandleGetWatchingFilesFromPair]
//  lazy val handleMasterWatchingFiles = mock[HandleMasterWatchingFiles]
//  lazy val fileTreeNodeDataFactory: FileTreeNodeData.Factory = mock[FileTreeNodeData.Factory]
//  lazy val createFileTree = mock[CreateFileTree]
//  lazy val handleMoveCaretEvent = mock[HandleMoveCaretEvent]
//  lazy val scrollToCaretInEditor = mock[ScrollToCaretInEditor]
//  lazy val convertEditorOffsetToPoint = mock[ConvertEditorOffsetToPoint]
//  lazy val drawCaretInEditor = mock[DrawCaretInEditor]
//  lazy val handleDocumentSnapshotEvent = mock[HandleDocumentSnapshotEvent]
//}
