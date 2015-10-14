package com.thoughtworks.pli.remotepair.idea

import akka.actor.{ActorSystem, Props}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.remotepair.actors.CoreActor
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.server_event_handlers._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest, PublishSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles, HandleWatchFilesChangedEvent}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.core.ui.dialogs._
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.models._
import com.thoughtworks.pli.remotepair.idea.statusbar.IdeaStatusBarWidget

class Module(currentIdeaRawProject: Project) {

  lazy val actorSystem = ActorSystem("IdeaPluginActorSystem", classLoader = Some(this.getClass.getClassLoader))

  lazy val myUtils = new MyUtils

  lazy val ideaLogger = Logger.getInstance(this.getClass)
  lazy val currentProject: IdeaProjectImpl = new IdeaProjectImpl(currentIdeaRawProject)(ideaFactories)

  lazy val runtimeAssertions = new RuntimeAssertions(logger)

  lazy val myClient = new MyClient(currentProject, myUtils, createFileTree, serverActor, logger)
  lazy val logger: PluginLogger = new PluginLogger(ideaLogger, myClient)
  lazy val clientVersionedDocuments = new ClientVersionedDocuments(currentProject, clientVersionedDocumentFactory)
  lazy val pairEventListeners = new PairEventListeners(currentProject, ideaIde)

  lazy val myIde = new IdeaIdeImpl(currentProject)
  lazy val myServer = new MyServer(currentProject, myIde, mySystem, logger, myClient)
  lazy val createFileTree = new CreateFileTree
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(myClient)
  lazy val mySystem = new MySystem
  lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProject, mySystem)
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(currentProject, tabEventsLocksInProject, mySystem, ideaIde)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(currentProject, ideaIde)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(logger, myClient, myUtils, mySystem)
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
  lazy val handleWatchFilesChangedEvent = new HandleWatchFilesChangedEvent(myClient, dialogFactories)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, myClient, currentProject, myUtils, handleChangeContentConfirmation, handleMoveCaretEvent, highlightPairSelection, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleCreatedProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, handleRenameDirEvent, handleRenameFileEvent, handleMoveDirEvent, handleMoveFileEvent, handleDocumentSnapshotEvent, handleWatchFilesChangedEvent, logger)
  lazy val myChannelHandlerFactory: MyChannelHandler.Factory = () => new MyChannelHandler(myClient, handleEvent, pairEventListeners, logger)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory: NettyClient.Factory = (serverAddress) => new NettyClient(serverAddress)(parseEvent, logger)
  lazy val handleIdeaFileEvent = new HandleIdeaFileEvent(currentProject, myIde, myClient, logger, clientVersionedDocuments)
  lazy val handleCaretChangeEvent = new HandleCaretChangeEvent(myClient, logger)
  lazy val handleDocumentChangeEvent = new HandleDocumentChangeEvent(myIde, myClient, logger, clientVersionedDocuments)
  lazy val handleSelectionEvent = new HandleSelectionEvent(myClient, logger)
  lazy val handleFileTabEvents = new HandleFileTabEvents(logger, myClient, tabEventsLocksInProject)

  lazy val coreActor = actorSystem.actorOf(Props(new CoreActor(handleCaretChangeEvent, handleDocumentChangeEvent, handleFileTabEvents, handleIdeaFileEvent, handleSelectionEvent)), "core")
  lazy val serverActor = actorSystem.actorSelection("akka.tcp://RemoteActorSystem@127.0.0.1:8888/user/PairServerActor")

  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(logger, coreActor, ideaFactories)
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(logger, coreActor, ideaFactories)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(logger, coreActor, ideaFactories)
  lazy val myFileEditorManagerFactory: MyFileEditorManager.Factory = () => new MyFileEditorManager(coreActor, logger, projectDocumentListenerFactory, projectCaretListenerFactory, projectSelectionListenerFactory, ideaFactories)
  lazy val myVirtualFileAdapterFactory: MyVirtualFileAdapter.Factory = () => new MyVirtualFileAdapter(currentProject, coreActor, myIde, myClient, logger, ideaFactories, myUtils)
  lazy val myProjectStorage = new IdeaProjectStorageImpl(currentProject)
  lazy val dialogFactories: DialogFactories = new DialogFactories {
    override def createWatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler]): VirtualWatchFilesDialog = new WatchFilesDialog(extraOnCloseHandler)(myIde: MyIde, myClient: MyClient, pairEventListeners: PairEventListeners, currentProject: IdeaProjectImpl, myUtils: MyUtils)
    override def createSyncFilesForMasterDialog: VirtualSyncFilesForMasterDialog = new SyncFilesForMasterDialog(currentProject: IdeaProjectImpl, myIde: MyIde, myClient: MyClient, dialogFactories, pairEventListeners: PairEventListeners)
    override def createConnectServerDialog: VirtualConnectServerDialog = new ConnectServerDialog(currentProject: IdeaProjectImpl, myProjectStorage: MyProjectStorage, myIde: MyIde, myUtils: MyUtils, pairEventListeners: PairEventListeners, myChannelHandlerFactory: MyChannelHandler.Factory, clientFactory: NettyClient.Factory, dialogFactories: DialogFactories, myClient: MyClient)
    override def createSyncFilesForSlaveDialog: VirtualSyncFilesForSlaveDialog = new SyncFilesForSlaveDialog(currentProject: IdeaProjectImpl, myClient: MyClient, dialogFactories, myIde: MyIde, pairEventListeners: PairEventListeners)
    override def createCopyProjectUrlDialog: VirtualCopyProjectUrlDialog = new CopyProjectUrlDialog(currentProject: IdeaProjectImpl, myIde: MyIde, myProjectStorage: MyProjectStorage, pairEventListeners: PairEventListeners, mySystem: MySystem, logger: PluginLogger)
    override def createProgressDialog: VirtualSyncProgressDialog = new SyncProgressDialog(currentProject: IdeaProjectImpl, myIde: MyIde, pairEventListeners: PairEventListeners)
  }

  lazy val ideaStatusWidgetFactory: IdeaStatusBarWidget.Factory = () => new IdeaStatusBarWidget(currentProject: IdeaProjectImpl, logger: PluginLogger, myClient: MyClient, myIde: IdeaIdeImpl, mySystem: MySystem, myProjectStorage: MyProjectStorage, myServer: MyServer, dialogFactories: DialogFactories)
  lazy val ideaFactories = new IdeaFactories(currentProject, myUtils)
  lazy val ideaIde = ideaFactories.platform
}
