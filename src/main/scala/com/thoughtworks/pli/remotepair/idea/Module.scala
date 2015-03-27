package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.core.files.{GetFileChildren, GetFileName, IsDirectory}
import com.thoughtworks.pli.remotepair.idea.core.tree.{CreateFileTree, FileTreeNodeDataFactory}
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidgetFactory
import com.thoughtworks.pli.remotepair.idea.utils._

trait UtilsModule {
  lazy val logger = Logger.getInstance(this.getClass)

  lazy val newUuid = new NewUuid

  lazy val runtimeAssertions = new RuntimeAssertions(isSubPath, logger)
  lazy val md5 = new Md5
  lazy val isSubPath = new IsSubPath
  lazy val getLocalHostName = new GetLocalHostName
  lazy val localIp = new GetLocalIp
  lazy val invokeLater = new InvokeLater
  lazy val runReadAction = new RunReadAction
  lazy val getIdeaProperties = new GetIdeaProperties
  lazy val clientNameInGlobalStorage = new ClientNameInGlobalStorage(getIdeaProperties, getLocalHostName)
  lazy val serverPortInGlobalStorage = new ServerPortInGlobalStorage(getIdeaProperties)
  lazy val standardizePath = new StandardizePath
  lazy val getFileContent = new GetFileContent
  lazy val getFileName = new GetFileName
  lazy val fileTreeNodeDataFactory = new FileTreeNodeDataFactory(getFileName)
  lazy val getFileChildren = new GetFileChildren
  lazy val isDirectory = new IsDirectory
  lazy val newHighlights = new NewHighlights
  lazy val removeOldHighlighters = new RemoveOldHighlighters
  lazy val getDocumentContent = new GetDocumentContent
  lazy val initListItems = new InitListItems
  lazy val getListItems = new GetListItems
  lazy val removeSelectedItemsFromList = new RemoveSelectedItemsFromList
  lazy val getFileOfEditor = new GetFileOfEditor
  lazy val getCachedFileContent = new GetCachedFileContent(getFileContent, getDocumentContent)
  lazy val synchronized = new Synchronized
  lazy val deleteFile = new DeleteFile
  lazy val fileExists = new FileExists
  lazy val getFilePath = new GetFilePath

}

trait Module extends UtilsModule {
  def currentProject: Project
  lazy val currentProjectScope = new CurrentProjectScope(currentProject, synchronized)
  lazy val connectionHolder = new ConnectionHolder(notifyChanges, currentProjectScope)
  lazy val channelHandlerHolder = new ChannelHandlerHolder(currentProjectScope)
  lazy val clientVersionedDocuments = new ClientVersionedDocuments(clientVersionedDocumentFactory, currentProjectScope)
  lazy val serverHolder = new ServerHolder(notifyChanges, currentProjectScope)
  lazy val pairEventListeners = new PairEventListeners(invokeLater, currentProjectScope)
  lazy val clientInfoHolder = new ClientInfoHolder(notifyChanges, currentProjectScope)
  lazy val serverStatusHolder = new ServerStatusHolder(notifyChanges, currentProjectScope)

  lazy val getMessageBus = new GetMessageBus(currentProject)
  lazy val notifyChanges = new NotifyChanges(getMessageBus)
  lazy val publishEvent = new PublishEvent(connectionHolder)
  lazy val runWriteAction = new RunWriteAction(currentProject)
  lazy val getCurrentProjectProperties = new GetCurrentProjectProperties(currentProject)
  lazy val targetProjectNameInProjectStorage = new ProjectNameInProjectStorage(getCurrentProjectProperties)
  lazy val serverPortInProjectStorage = new ServerPortInProjectStorage(getCurrentProjectProperties)
  lazy val serverHostInProjectStorage = new ServerHostInProjectStorage(getCurrentProjectProperties)
  lazy val showErrorDialog = new ShowErrorDialog(currentProject)
  lazy val showMessageDialog = new ShowMessageDialog(currentProject)
  lazy val startServer = new StartServer(currentProject, invokeLater, localIp, serverPortInGlobalStorage, logger, serverHolder, showMessageDialog, showErrorDialog)
  lazy val getMyClientId = new GetMyClientId(clientInfoHolder)
  lazy val getProjectInfoData = new GetProjectInfoData(serverStatusHolder, clientInfoHolder)
  lazy val getServerWatchingFiles = new GetServerWatchingFiles(getProjectInfoData)
  lazy val getProjectBaseDir = new GetProjectBaseDir(currentProject)
  lazy val getProjectBasePath = new GetProjectBasePath(getProjectBaseDir, standardizePath)
  lazy val getRelativePath = new GetRelativePath(getProjectBasePath, runtimeAssertions, isSubPath, standardizePath)
  lazy val getFileSummary = new GetFileSummary(getRelativePath, getFileContent, md5)
  lazy val createFileTree = new CreateFileTree(getRelativePath, isDirectory, getFileChildren, fileTreeNodeDataFactory)
  lazy val isWatching = new IsWatching(getRelativePath)
  lazy val getAllWatchingFiles = new GetAllWatchingFiles(getProjectBaseDir, getServerWatchingFiles, createFileTree, isWatching)
  lazy val getWatchingFileSummaries = new GetWatchingFileSummaries(getAllWatchingFiles, getFileSummary)
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(getWatchingFileSummaries, publishEvent, getServerWatchingFiles, getMyClientId)
  lazy val getFileEditorManager = new GetFileEditorManager(currentProject)
  lazy val closeFile = new CloseFile(getFileEditorManager)
  lazy val getOpenFileDescriptor = new GetOpenFileDescriptor(currentProject)
  lazy val getFileByRelative = new GetFileByRelative(runtimeAssertions, getProjectBaseDir)
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(getFileByRelative, invokeLater, publishEvent, getOpenFileDescriptor)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(getFileByRelative, invokeLater, closeFile)
  lazy val publishCreateDocumentEvent = new PublishCreateDocumentEvent(publishEvent, getRelativePath, clientInfoHolder, getFileContent)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(logger, publishEvent, newUuid)
  lazy val getEditorsOfPath = new GetEditorsOfPath(getFileByRelative, getFileEditorManager)
  lazy val getTextEditorsOfPath = new GetTextEditorsOfPath(getEditorsOfPath)
  lazy val findOrCreateDir = new FindOrCreateDir(runtimeAssertions, getProjectBaseDir)
  lazy val findOrCreateFile = new FindOrCreateFile(currentProject, findOrCreateDir, runtimeAssertions)
  lazy val writeToProjectFile = new WriteToProjectFile(getTextEditorsOfPath, findOrCreateFile)
  lazy val highlightNewContent = new HighlightNewContent(getTextEditorsOfPath, newHighlights, removeOldHighlighters, getDocumentContent)
  lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(publishEvent, runWriteAction, logger, clientVersionedDocuments, getFileByRelative, writeToProjectFile, getCachedFileContent, getFileContent, highlightNewContent, synchronized)
  lazy val getEditorPath = new GetEditorPath(getFileOfEditor, getRelativePath)
  lazy val getSelectedTextEditor = new GetSelectedTextEditor(getFileEditorManager)
  lazy val isCaretSharing = new IsCaretSharing(getProjectInfoData)
  lazy val convertEditorOffsetToPoint = new ConvertEditorOffsetToPoint()
  lazy val scrollToCaretInEditor = new ScrollToCaretInEditor(convertEditorOffsetToPoint)
  lazy val drawCaretInEditor = new DrawCaretInEditor(convertEditorOffsetToPoint)
  lazy val moveCaret = new HandleMoveCaretEvent(invokeLater, getTextEditorsOfPath, isCaretSharing, scrollToCaretInEditor, convertEditorOffsetToPoint, drawCaretInEditor)
  lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(runReadAction, publishEvent, getFileByRelative, getFileContent)
  lazy val highlightPairSelection = new HighlightPairSelection(getTextEditorsOfPath, invokeLater, publishEvent, newHighlights, removeOldHighlighters, logger)
  lazy val handleSyncFilesRequest = new HandleSyncFilesRequest(getAllWatchingFiles, publishEvent, getMyClientId, getRelativePath, getFileContent, getFileSummary)
  lazy val handleMasterWatchingFiles = new HandleMasterWatchingFiles(getRelativePath, getAllWatchingFiles, invokeLater, runWriteAction, logger, deleteFile, fileExists, getFilePath)
  lazy val handleCreateDocumentConfirmation = new HandleCreateDocumentConfirmation(writeToProjectFile, runWriteAction, clientVersionedDocuments)
  lazy val handleGetPairableFilesFromPair = new HandleGetWatchingFilesFromPair(getMyClientId, publishEvent, getWatchingFileSummaries)
  lazy val getOpenedFiles = new GetOpenedFiles(getFileEditorManager)
  lazy val handleServerStatusResponse = new HandleServerStatusResponse(serverStatusHolder)
  lazy val handleJoinedToProjectEvent = new HandleJoinedToProjectEvent(getOpenedFiles, publishCreateDocumentEvent)
  lazy val handleSyncFilesForAll = new HandleSyncFilesForAll(invokeLater, publishSyncFilesRequest)
  lazy val handleSyncFileEvent = new HandleSyncFileEvent(writeToProjectFile, runWriteAction)
  lazy val deleteProjectFile = new DeleteProjectFile(getFileByRelative, runtimeAssertions)
  lazy val handleCreateDirEvent = new HandleCreateDirEvent(findOrCreateDir, runWriteAction)
  lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(deleteProjectFile, runWriteAction)
  lazy val deleteProjectDir = new DeleteProjectDir(runtimeAssertions, getFileByRelative)
  lazy val handleClientInfoResponse = new HandleClientInfoResponse(clientInfoHolder)
  lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(runWriteAction, deleteProjectDir)
  lazy val handleCreateFileEvent = new HandleCreateFileEvent(runWriteAction, writeToProjectFile)
  lazy val showServerError = new ShowServerError(showErrorDialog)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, runWriteAction, publishCreateDocumentEvent, publishEvent, handleChangeContentConfirmation, publishSyncFilesRequest, moveCaret, highlightPairSelection, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, showServerError, invokeLater, logger, md5)
  lazy val connectionFactory = new ConnectionFactory(logger)
  lazy val myChannelHandlerFactory = new MyChannelHandlerFactory(connectionHolder, handleEvent, pairEventListeners, connectionFactory, logger)
  lazy val getSelectedFromFileTree = new GetSelectedFromFileTree(getRelativePath)
  lazy val resetTreeWithExpandedPathKept = new ResetTreeWithExpandedPathKept
  lazy val initFileTree = new InitFileTree(resetTreeWithExpandedPathKept, createFileTree, getProjectBaseDir: GetProjectBaseDir)
  lazy val getExistingProjects = new GetExistingProjects(serverStatusHolder: ServerStatusHolder)
  lazy val removeDuplicatePaths = new RemoveDuplicatePaths(isSubPath: IsSubPath)
  lazy val getProjectWindow = new GetProjectWindow(currentProject: Project)
  lazy val watchFilesDialogFactory = new WatchFilesDialogFactory(invokeLater, publishEvent, pairEventListeners, isSubPath, getServerWatchingFiles, getSelectedFromFileTree, getListItems, removeSelectedItemsFromList, removeDuplicatePaths, initListItems, initFileTree, getProjectWindow, showErrorDialog, isWatching: IsWatching)
  lazy val joinProjectDialogFactory = new JoinProjectDialogFactory(invokeLater, watchFilesDialogFactory, pairEventListeners, logger, publishEvent, showServerError, getExistingProjects, clientNameInGlobalStorage, getProjectWindow, getServerWatchingFiles: GetServerWatchingFiles)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory = new ClientFactory(parseEvent, logger: Logger)
  lazy val connectServerDialogFactory = new ConnectServerDialogFactory(joinProjectDialogFactory, invokeLater, pairEventListeners, myChannelHandlerFactory, clientFactory, serverHostInProjectStorage, serverPortInProjectStorage, getProjectWindow, channelHandlerHolder: ChannelHandlerHolder)
  lazy val inWatchingList = new InWatchingList(getServerWatchingFiles, isSubPath, getRelativePath: GetRelativePath)
  lazy val getUserData = new GetUserData
  lazy val putUserData = new PutUserData
  lazy val getCaretOffset = new GetCaretOffset
  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(publishEvent, logger, inWatchingList, getDocumentContent, getUserData, putUserData, getRelativePath, getCaretOffset)
  lazy val getSelectionEventInfo = new GetSelectionEventInfo
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(publishEvent, logger, inWatchingList, getRelativePath, getSelectionEventInfo)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(invokeLater, publishEvent, publishCreateDocumentEvent, newUuid, logger, clientVersionedDocuments, inWatchingList, getRelativePath, getDocumentContent, getCaretOffset)
  lazy val myFileEditorManagerFactory = new MyFileEditorManagerFactory(projectCaretListenerFactory, publishCreateDocumentEvent, projectDocumentListenerFactory, projectSelectionListenerFactory, logger, publishEvent, getRelativePath: GetRelativePath)
  lazy val containsProjectFile = new ContainsProjectFile(getProjectBasePath, isSubPath: IsSubPath)
  lazy val myVirtualFileAdapterFactory = new MyVirtualFileAdapterFactory(invokeLater, publishEvent, logger, containsProjectFile, getRelativePath, getFileContent, getCachedFileContent: GetCachedFileContent)
  lazy val clientIdToName = new ClientIdToName(getProjectInfoData: GetProjectInfoData)
  lazy val getAllClients = new GetAllClients(getProjectInfoData: GetProjectInfoData)
  lazy val getMasterClientId = new GetMasterClientId(getProjectInfoData: GetProjectInfoData)
  lazy val syncFilesForSlaveDialogFactory = new SyncFilesForSlaveDialogFactory(clientIdToName, watchFilesDialogFactory, invokeLater, pairEventListeners, getProjectWindow, getWatchingFileSummaries, connectionHolder, getMyClientId, getMasterClientId, getAllClients: GetAllClients)
  lazy val getOtherClients = new GetOtherClients(getAllClients, getMyClientId: GetMyClientId)
  lazy val syncFilesForMasterDialogFactory = new SyncFilesForMasterDialogFactory(connectionHolder, watchFilesDialogFactory, clientIdToName, invokeLater, pairEventListeners, getProjectWindow, getMyClientId, getOtherClients, getWatchingFileSummaries: GetWatchingFileSummaries)
  lazy val amIMaster = new AmIMaster(clientInfoHolder: ClientInfoHolder)
  lazy val closeConnection = new CloseConnection(connectionHolder: ConnectionHolder)
  lazy val statusWidgetPopups = new StatusWidgetPopups(connectionHolder, invokeLater, publishEvent, localIp, syncFilesForMasterDialogFactory, syncFilesForSlaveDialogFactory, getAllClients, getProjectInfoData, isCaretSharing, serverHolder, showErrorDialog, amIMaster, closeConnection: CloseConnection)
  lazy val createMessageConnection = new CreateMessageConnection(getMessageBus, currentProject: Project)
  lazy val pairStatusWidgetFactory = new PairStatusWidgetFactory(statusWidgetPopups, logger, serverHolder, amIMaster, createMessageConnection, isCaretSharing, connectionHolder: ConnectionHolder)
  lazy val getStatusBar = new GetStatusBar(currentProject: Project)

}
