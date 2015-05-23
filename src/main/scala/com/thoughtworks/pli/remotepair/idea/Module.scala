package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server_event_handlers._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest, PublishSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles, HandleWatchFilesChangedEvent}
import com.thoughtworks.pli.remotepair.core.utils.{CreateFileTree, FileTreeNodeData}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.models._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget

trait UtilsModule {
  lazy val newUuid = new NewUuid
  lazy val ideaLogger = Logger.getInstance(this.getClass)

  lazy val md5 = new Md5
  lazy val isSubPath = new IsSubPath
  lazy val fileTreeNodeDataFactory: FileTreeNodeData.Factory = (file) => new FileTreeNodeData(file)
}

trait Module extends UtilsModule {
  def currentIdeaProject: Project

  lazy val currentProject: IdeaProjectImpl = new IdeaProjectImpl(currentIdeaProject)(ideaFactories)

  lazy val runtimeAssertions = new RuntimeAssertions(isSubPath, logger)

  lazy val myClient = new MyClient(currentProject, isSubPath, createFileTree, logger)
  lazy val logger: PluginLogger = new PluginLogger(ideaLogger, myClient)
  lazy val clientVersionedDocuments = new ClientVersionedDocuments(currentProject, clientVersionedDocumentFactory)
  lazy val pairEventListeners = new PairEventListeners(currentProject, ideaIde)

  lazy val myIde = new IdeaIdeImpl(currentProject)
  lazy val startServer = new StartServer(currentProject, myIde, mySystem, logger, myClient)
  lazy val createFileTree = new CreateFileTree(fileTreeNodeDataFactory)
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(myClient)
  lazy val mySystem = new MySystem
  lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProject, mySystem)
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(currentProject, tabEventsLocksInProject, mySystem, ideaIde)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(currentProject, ideaIde)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(logger, myClient, newUuid, mySystem)
  lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(currentProject, myClient, ideaIde, logger, clientVersionedDocuments)
  lazy val handleMoveCaretEvent = new HandleMoveCaretEvent(currentProject, ideaIde, myClient)
  lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(currentProject, ideaIde, myClient)
  lazy val highlightPairSelection = new HighlightPairSelection(currentProject, ideaIde, myClient, logger)
  lazy val handleSyncFilesRequest = new HandleSyncFilesRequest(myClient)
  lazy val handleMasterWatchingFiles = new HandleMasterWatchingFiles(myClient, ideaIde, logger)
  lazy val handleCreateDocumentConfirmation = new HandleCreateDocumentConfirmation(currentProject, ideaIde, clientVersionedDocuments)
  lazy val handleGetPairableFilesFromPair = new HandleGetWatchingFilesFromPair(myClient)
  lazy val handleServerStatusResponse = new HandleServerStatusResponse(myClient)
  lazy val handleJoinedToProjectEvent = new HandleJoinedToProjectEvent(currentProject, ideaIde)
  lazy val handleSyncFilesForAll = new HandleSyncFilesForAll(ideaIde, publishSyncFilesRequest)
  lazy val handleSyncFileEvent = new HandleSyncFileEvent(currentProject, ideaIde)
  lazy val handleCreateDirEvent = new HandleCreateDirEvent(currentProject, ideaIde, logger)
  lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(currentProject, ideaIde, logger)
  lazy val handleClientInfoResponse = new HandleClientInfoResponse(myClient)
  lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(currentProject, ideaIde, logger)
  lazy val handleCreateFileEvent = new HandleCreateFileEvent(currentProject, ideaIde, logger)
  lazy val handleCreatedProjectEvent = new HandleCreatedProjectEvent()
  lazy val handleDocumentSnapshotEvent = new HandleDocumentSnapshotEvent(currentProject, clientVersionedDocuments, logger, ideaIde)
  lazy val handleRenameDirEvent = new HandleRenameDirEvent(currentProject, ideaIde, logger)
  lazy val handleRenameFileEvent = new HandleRenameFileEvent(currentProject, ideaIde, logger)
  lazy val handleMoveDirEvent = new HandleMoveDirEvent(currentProject, ideaIde, logger)
  lazy val handleMoveFileEvent = new HandleMoveFileEvent(currentProject, ideaIde, logger)
  lazy val handleWatchFilesChangedEvent = new HandleWatchFilesChangedEvent(myClient, syncFilesForSlaveDialogFactory)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, myClient, currentProject, handleChangeContentConfirmation, handleMoveCaretEvent, highlightPairSelection, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleCreatedProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, handleRenameDirEvent, handleRenameFileEvent, handleMoveDirEvent, handleMoveFileEvent, handleDocumentSnapshotEvent, handleWatchFilesChangedEvent, logger, md5)
  lazy val myChannelHandlerFactory: MyChannelHandler.Factory = () => new MyChannelHandler(myClient, handleEvent, pairEventListeners, logger)
  lazy val watchFilesDialogFactory: WatchFilesDialog.Factory = (extraOnCloseHandler) => new WatchFilesDialog(extraOnCloseHandler)(myIde, myClient, pairEventListeners, isSubPath, currentProject, createFileTree)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory: NettyClient.Factory = (serverAddress) => new NettyClient(serverAddress)(parseEvent, logger)
  lazy val copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory = () => new CopyProjectUrlDialog(currentProject, myIde, ideaProjectStorage, pairEventListeners, mySystem, logger)
  lazy val connectServerDialogFactory: ConnectServerDialog.Factory = () => new ConnectServerDialog(currentProject, ideaProjectStorage, myIde, pairEventListeners, myChannelHandlerFactory, clientFactory, newUuid, watchFilesDialogFactory, copyProjectUrlDialogFactory, syncFilesForSlaveDialogFactory, myClient)
  lazy val handleIdeaFileEvent = new HandleIdeaFileEvent(currentProject, myIde, myClient, logger, clientVersionedDocuments)
  lazy val handleCaretChangeEvent = new HandleCaretChangeEvent(myClient, logger)
  lazy val handleDocumentChangeEvent = new HandleDocumentChangeEvent(myIde, myClient, newUuid, logger, clientVersionedDocuments)
  lazy val handleSelectionEvent = new HandleSelectionEvent(myClient, logger)
  lazy val handleFileTabEvents = new HandleFileTabEvents(logger, myClient, tabEventsLocksInProject)
  lazy val handleIdeaEvent = new HandleIdeaEvent(handleCaretChangeEvent, handleDocumentChangeEvent, handleFileTabEvents, handleIdeaFileEvent, handleSelectionEvent)
  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val myFileEditorManagerFactory: MyFileEditorManager.Factory = () => new MyFileEditorManager(handleIdeaEvent, logger, projectDocumentListenerFactory, projectCaretListenerFactory, projectSelectionListenerFactory, ideaFactories)
  lazy val myVirtualFileAdapterFactory: MyVirtualFileAdapter.Factory = () => new MyVirtualFileAdapter(currentProject, handleIdeaEvent, myIde, myClient, logger, isSubPath, ideaFactories)
  lazy val syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory = () => new SyncFilesForSlaveDialog(currentProject, myClient, watchFilesDialogFactory, myIde, pairEventListeners)
  lazy val syncFilesForMasterDialogFactory: SyncFilesForMasterDialog.Factory = () => new SyncFilesForMasterDialog(currentProject, myIde, myClient, watchFilesDialogFactory, pairEventListeners)
  lazy val ideaProjectStorage = new IdeaProjectStorageImpl(currentProject)
  lazy val copyProjectUrlToClipboard = new CopyProjectUrlToClipboard(ideaProjectStorage, mySystem)
  lazy val statusWidgetPopups = new StatusWidgetPopups(currentProject, myClient, ideaIde, mySystem, syncFilesForMasterDialogFactory, syncFilesForSlaveDialogFactory, copyProjectUrlToClipboard)
  lazy val pairStatusWidgetFactory: PairStatusWidget.Factory = () => new PairStatusWidget(currentProject, statusWidgetPopups, logger, myClient)
  lazy val ideaFactories = new IdeaFactories(currentProject, md5)
  lazy val ideaIde = ideaFactories.platform
}
