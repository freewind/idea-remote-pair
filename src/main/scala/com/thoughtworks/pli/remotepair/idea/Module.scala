package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
import com.thoughtworks.pli.remotepair.core.models.{IdeaFactories, MyIde, MyProject, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.server_event_handlers._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest, PublishSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleWatchFilesChangedEvent, HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.statusbar.IdeaStatusBarWidget

trait Module {
  def currentIdeaRawProject: Project

  lazy val myUtils = new MyUtils

  lazy val ideaLogger = Logger.getInstance(this.getClass)
  lazy val currentProject: MyProject = new MyProject(currentIdeaRawProject)(ideaFactories, logger)

  lazy val runtimeAssertions = new RuntimeAssertions(logger)

  lazy val myClient = new MyClient(currentProject, myUtils, createFileTree, logger)
  lazy val logger: PluginLogger = new PluginLogger(ideaLogger, myClient)
  lazy val clientVersionedDocuments = new ClientVersionedDocuments(currentProject, clientVersionedDocumentFactory)
  lazy val pairEventListeners = new PairEventListeners(currentProject, ideaIde)

  lazy val myIde = new MyIde(currentProject)
  lazy val myServer = new MyServer(currentProject, myIde, mySystem, logger, myClient)
  lazy val createFileTree = new CreateFileTree
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(myClient)
  lazy val mySystem = new MySystem
  lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProject, mySystem)
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(currentProject, tabEventsLocksInProject, mySystem, ideaIde)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(currentProject, ideaIde)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = docInfo => new ClientVersionedDocument(docInfo.path, docInfo.version, docInfo.content)(logger, myClient, myUtils, mySystem)
  lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(currentProject, myClient, ideaIde, logger, clientVersionedDocuments)
  lazy val pairCarets = new PairCarets(currentProject, ideaIde, myClient)
  lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(currentProject, ideaIde, myClient)
  lazy val pairSelections = new PairSelections(currentProject, ideaIde, myClient, logger)
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
  lazy val handleWatchFilesChangedEvent = new HandleWatchFilesChangedEvent(myClient, dialogFactories)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, myClient, currentProject, myUtils, handleChangeContentConfirmation, pairCarets, pairSelections, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleCreatedProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, handleRenameDirEvent, handleRenameFileEvent, handleMoveDirEvent, handleMoveFileEvent, handleDocumentSnapshotEvent, handleWatchFilesChangedEvent, logger)
  lazy val myChannelHandlerFactory: MyChannelHandler.Factory = () => new MyChannelHandler(myClient, handleEvent, pairEventListeners, logger)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory: NettyClient.Factory = (serverAddress) => new NettyClient(serverAddress)(parseEvent, logger)
  lazy val handleIdeaFileEvent = new HandleIdeaFileEvent(currentProject, myIde, myClient, logger, clientVersionedDocuments)
  lazy val handleCaretChangeEvent = new HandleCaretChangeEvent(myClient, logger)
  lazy val handleDocumentChangeEvent = new HandleDocumentChangeEvent(myIde, myClient, logger, clientVersionedDocuments)
  lazy val handleSelectionEvent = new HandleSelectionEvent(myClient, logger)
  lazy val handleFileTabEvents = new HandleFileTabEvents(logger, myClient, tabEventsLocksInProject)
  lazy val handleIdeaEvent = new HandleIdeaEvent(handleCaretChangeEvent, handleDocumentChangeEvent, handleFileTabEvents, handleIdeaFileEvent, handleSelectionEvent)
  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(logger, handleIdeaEvent, ideaFactories)
  lazy val myFileEditorManagerFactory: MyFileEditorManager.Factory = () => new MyFileEditorManager(handleIdeaEvent, logger, projectDocumentListenerFactory, projectCaretListenerFactory, projectSelectionListenerFactory, ideaFactories)
  lazy val myVirtualFileAdapterFactory: MyVirtualFileAdapter.Factory = () => new MyVirtualFileAdapter(currentProject, handleIdeaEvent, myIde, myClient, logger, ideaFactories, myUtils)
  lazy val myProjectStorage = new MyProjectStorage(currentProject)
  lazy val dialogFactories: DialogFactories = new DialogFactories {
    override def createWatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler]): WatchFilesDialog = new WatchFilesDialog(extraOnCloseHandler)(myIde: MyIde, myClient: MyClient, pairEventListeners: PairEventListeners, currentProject: MyProject, myUtils: MyUtils)
    override def createSyncFilesForMasterDialog: SyncFilesForMasterDialog = new SyncFilesForMasterDialog(currentProject: MyProject, myIde: MyIde, myClient: MyClient, dialogFactories, pairEventListeners: PairEventListeners)
    override def createConnectServerDialog: ConnectServerDialog = new ConnectServerDialog(currentProject: MyProject, myProjectStorage: MyProjectStorage, myIde: MyIde, myUtils: MyUtils, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, dialogFactories: DialogFactories, myClient: MyClient)
    override def createSyncFilesForSlaveDialog: SyncFilesForSlaveDialog = new SyncFilesForSlaveDialog(currentProject: MyProject, myClient: MyClient, dialogFactories, myIde: MyIde, pairEventListeners: PairEventListeners)
    override def createCopyProjectUrlDialog: CopyProjectUrlDialog = new CopyProjectUrlDialog(currentProject: MyProject, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
    override def createProgressDialog: SyncProgressDialog = new SyncProgressDialog(currentProject: MyProject, myIde: MyIde, pairEventListeners: PairEventListeners)
  }

  lazy val ideaStatusWidgetFactory: IdeaStatusBarWidget.Factory = () => new IdeaStatusBarWidget(currentProject: MyProject, logger: PluginLogger, myClient: MyClient, myIde: MyIde, mySystem: MySystem, myProjectStorage: MyProjectStorage, myServer: MyServer, dialogFactories: DialogFactories)
  lazy val ideaFactories = new IdeaFactories(currentProject, myUtils, logger)
  lazy val ideaIde = ideaFactories.platform
}
