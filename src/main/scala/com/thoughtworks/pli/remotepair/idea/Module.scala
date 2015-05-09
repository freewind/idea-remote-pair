package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.core.files.{GetFileChildren, GetFileName, IsDirectory}
import com.thoughtworks.pli.remotepair.idea.core.tree.{CreateFileTree, FileTreeNodeData}
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners._
import com.thoughtworks.pli.remotepair.idea.settings._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget
import com.thoughtworks.pli.remotepair.idea.utils._

trait UtilsModule {
  lazy val newUuid = new NewUuid
  lazy val logger = Logger.getInstance(this.getClass)

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
  lazy val fileTreeNodeDataFactory: FileTreeNodeData.Factory = (file: VirtualFile) => new FileTreeNodeData(file)(getFileName)
  lazy val getFileChildren = new GetFileChildren
  lazy val isDirectory = new IsDirectory
  lazy val newHighlights = new NewHighlights
  lazy val removeOldHighlighters = new RemoveOldHighlighters
  lazy val getDocumentContent = new GetDocumentContent
  lazy val initListItems = new InitListItems
  lazy val getListItems = new GetListItems
  lazy val removeSelectedItemsFromList = new RemoveSelectedItemsFromList
  lazy val getCachedFileContent = new GetCachedFileContent(getFileContent, getDocumentContent)
  lazy val synchronized = new Synchronized
  lazy val deleteFile = new DeleteFile
  lazy val fileExists = new FileExists
  lazy val getFilePath = new GetFilePath

}

trait Module extends UtilsModule {
  def currentProject: Project

  lazy val runtimeAssertions = new RuntimeAssertions(isSubPath, pluginLogger)

  lazy val pluginLogger = new PluginLogger(logger, getMyClientName)
  lazy val currentProjectScope = new CurrentProjectScope(currentProject, synchronized)
  lazy val connectionHolder = new ConnectionHolder(notifyChanges, currentProjectScope)
  lazy val channelHandlerHolder = new ChannelHandlerHolder(notifyChanges, currentProjectScope)
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
  lazy val startServer = new StartServer(currentProject, invokeLater, localIp, serverPortInGlobalStorage, pluginLogger, serverHolder, showMessageDialog, showErrorDialog)
  lazy val getMyClientId = new GetMyClientId(clientInfoHolder)
  lazy val getProjectInfoData = new GetProjectInfoData(serverStatusHolder, clientInfoHolder)
  lazy val getServerWatchingFiles = new GetServerWatchingFiles(getProjectInfoData)
  lazy val getProjectBaseDir = new GetProjectBaseDir(currentProject)
  lazy val getProjectBasePath = new GetProjectBasePath(getProjectBaseDir, standardizePath)
  lazy val getRelativePath = new GetRelativePath(getProjectBasePath, runtimeAssertions, isSubPath, standardizePath)
  lazy val getFileSummary = new GetFileSummary(getRelativePath, getFileContent, md5)
  lazy val createFileTree = new CreateFileTree(getRelativePath, isDirectory, getFileChildren, fileTreeNodeDataFactory)
  lazy val isInPathList = new IsInPathList(getRelativePath)
  lazy val getAllWatchingFiles = new GetAllWatchingFiles(getProjectBaseDir, getServerWatchingFiles, createFileTree, isInPathList)
  lazy val getWatchingFileSummaries = new GetWatchingFileSummaries(getAllWatchingFiles, getFileSummary)
  lazy val publishSyncFilesRequest = new PublishSyncFilesRequest(getWatchingFileSummaries, publishEvent, getServerWatchingFiles, getMyClientId)
  lazy val getFileEditorManager = new GetFileEditorManager(currentProject)
  lazy val closeFile = new CloseFile(getFileEditorManager)
  lazy val getOpenFileDescriptor = new GetOpenFileDescriptor(currentProject)
  lazy val getFileByRelative = new GetFileByRelative(runtimeAssertions, getProjectBaseDir)
  lazy val openFileInTab = new OpenFileInTab(getOpenFileDescriptor, invokeLater, findOrCreateFile)
  lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProjectScope, getCurrentTimeMillis)
  lazy val getCurrentTimeMillis = new GetCurrentTimeMillis()
  lazy val isFileOpened = new IsFileOpened(getFileEditorManager)
  lazy val isFileInActiveTab = new IsFileInActiveTab(getFileEditorManager)
  lazy val handleOpenTabEvent = new HandleOpenTabEvent(getFileByRelative, openFileInTab, tabEventsLocksInProject, getCurrentTimeMillis, isFileInActiveTab)
  lazy val handleCloseTabEvent = new HandleCloseTabEvent(getFileByRelative, invokeLater, closeFile)
  lazy val publishCreateDocumentEvent = new PublishCreateDocumentEvent(publishEvent, getRelativePath, getFileContent)
  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(pluginLogger, publishEvent, newUuid, getCurrentTimeMillis)
  lazy val getEditorsOfPath = new GetEditorsOfPath(getFileByRelative, getFileEditorManager)
  lazy val getTextEditorsOfPath = new GetTextEditorsOfPath(getEditorsOfPath)
  lazy val createChildDirectory = new CreateChildDirectory()
  lazy val findChild = new FindChild()
  lazy val findOrCreateDir = new FindOrCreateDir(runtimeAssertions, getProjectBaseDir, createChildDirectory, findChild)
  lazy val findOrCreateFile = new FindOrCreateFile(currentProject, findOrCreateDir, runtimeAssertions)
  lazy val writeToProjectFile = new WriteToProjectFile(getTextEditorsOfPath, findOrCreateFile)
  lazy val highlightNewContent = new HighlightNewContent(getTextEditorsOfPath, newHighlights, removeOldHighlighters, getDocumentContent)
  lazy val getMyClientName = new GetMyClientName(clientInfoHolder)
  lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(publishEvent, runWriteAction, pluginLogger, clientVersionedDocuments, getFileByRelative, writeToProjectFile, getCachedFileContent, getFileContent, highlightNewContent, synchronized, getMyClientId, getMyClientName)
  lazy val isCaretSharing = new IsCaretSharing(getProjectInfoData)
  lazy val convertEditorOffsetToPoint = new ConvertEditorOffsetToPoint()
  lazy val scrollToCaretInEditor = new ScrollToCaretInEditor(convertEditorOffsetToPoint)
  lazy val drawCaretInEditor = new DrawCaretInEditor(convertEditorOffsetToPoint)
  lazy val moveCaret = new HandleMoveCaretEvent(invokeLater, getTextEditorsOfPath, isCaretSharing, scrollToCaretInEditor, convertEditorOffsetToPoint, drawCaretInEditor)
  lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(runReadAction, publishEvent, getFileByRelative, getFileContent)
  lazy val getDocumentLength = new GetDocumentLength(getDocumentContent)
  lazy val highlightPairSelection = new HighlightPairSelection(getTextEditorsOfPath, invokeLater, publishEvent, newHighlights, removeOldHighlighters, pluginLogger, getDocumentLength)
  lazy val handleSyncFilesRequest = new HandleSyncFilesRequest(getAllWatchingFiles, publishEvent, getMyClientId, getRelativePath, getFileContent, getFileSummary, amIMaster)
  lazy val handleMasterWatchingFiles = new HandleMasterWatchingFiles(getRelativePath, getAllWatchingFiles, invokeLater, runWriteAction, pluginLogger, deleteFile, fileExists, getFilePath)
  lazy val handleCreateDocumentConfirmation = new HandleCreateDocumentConfirmation(writeToProjectFile, runWriteAction, clientVersionedDocuments)
  lazy val handleGetPairableFilesFromPair = new HandleGetWatchingFilesFromPair(getMyClientId, publishEvent, getWatchingFileSummaries)
  lazy val getOpenedFiles = new GetOpenedFiles(getFileEditorManager)
  lazy val handleServerStatusResponse = new HandleServerStatusResponse(serverStatusHolder)
  lazy val handleJoinedToProjectEvent = new HandleJoinedToProjectEvent(getOpenedFiles, runWriteAction, closeFile)
  lazy val handleSyncFilesForAll = new HandleSyncFilesForAll(invokeLater, publishSyncFilesRequest)
  lazy val handleSyncFileEvent = new HandleSyncFileEvent(writeToProjectFile, runWriteAction)
  lazy val handleCreateDirEvent = new HandleCreateDirEvent(findOrCreateDir, runWriteAction, pluginLogger)
  lazy val handleDeleteFileEvent = new HandleDeleteFileEvent(runWriteAction, getFileByRelative, deleteFile, pluginLogger)
  lazy val handleClientInfoResponse = new HandleClientInfoResponse(clientInfoHolder)
  lazy val handleDeleteDirEvent = new HandleDeleteDirEvent(runWriteAction, getFileByRelative, deleteFile, pluginLogger)
  lazy val handleCreateFileEvent = new HandleCreateFileEvent(runWriteAction, writeToProjectFile, pluginLogger)
  lazy val showServerError = new ShowServerError(showErrorDialog)
  lazy val handleCreatedProjectEvent = new HandleCreatedProjectEvent(getOpenedFiles, publishCreateDocumentEvent)
  lazy val handleDocumentSnapshotEvent = new HandleDocumentSnapshotEvent(clientVersionedDocuments, pluginLogger, getMyClientName, writeToProjectFile, runWriteAction, openFileInTab)
  lazy val handleRenameDirEvent = new HandleRenameDirEvent(getFileByRelative, runWriteAction, pluginLogger)
  lazy val handleRenameFileEvent = new HandleRenameFileEvent(getFileByRelative, runWriteAction, pluginLogger)
  lazy val handleMoveDirEvent = new HandleMoveDirEvent(getFileByRelative, runWriteAction, pluginLogger)
  lazy val handleMoveFileEvent = new HandleMoveFileEvent(getFileByRelative, runWriteAction, pluginLogger)
  lazy val handleWatchFilesChangedEvent = new HandleWatchFilesChangedEvent(amIMaster, syncFilesForSlaveDialogFactory)
  lazy val handleEvent = new HandleEvent(handleOpenTabEvent, handleCloseTabEvent, runWriteAction, publishCreateDocumentEvent, publishEvent, handleChangeContentConfirmation, moveCaret, highlightPairSelection, handleSyncFilesRequest, handleMasterWatchingFiles, handleCreateServerDocumentRequest, handleCreateDocumentConfirmation, handleGetPairableFilesFromPair, handleJoinedToProjectEvent, handleCreatedProjectEvent, handleServerStatusResponse, handleClientInfoResponse, handleSyncFilesForAll, handleSyncFileEvent, handleCreateDirEvent, handleDeleteFileEvent, handleDeleteDirEvent, handleCreateFileEvent, handleRenameDirEvent, handleRenameFileEvent, handleMoveDirEvent, handleMoveFileEvent, handleDocumentSnapshotEvent, handleWatchFilesChangedEvent, showServerError, invokeLater, pluginLogger, md5)
  lazy val connectionFactory: Connection.Factory = (channelHandlerContext) => new Connection(channelHandlerContext)(pluginLogger)
  lazy val myChannelHandlerFactory: MyChannelHandler.Factory = () => new MyChannelHandler(connectionHolder, handleEvent, pairEventListeners, connectionFactory, pluginLogger)
  lazy val getSelectedFromFileTree = new GetSelectedFromFileTree(getRelativePath)
  lazy val resetTreeWithExpandedPathKept = new ResetTreeWithExpandedPathKept
  lazy val initFileTree = new InitFileTree(resetTreeWithExpandedPathKept, createFileTree, getProjectBaseDir)
  lazy val getExistingProjects = new GetExistingProjects(serverStatusHolder)
  lazy val removeDuplicatePaths = new RemoveDuplicatePaths(isSubPath)
  lazy val getProjectWindow = new GetProjectWindow(currentProject)
  lazy val watchFilesDialogFactory: WatchFilesDialog.Factory = (extraOnCloseHandler) => new WatchFilesDialog(extraOnCloseHandler)(invokeLater, publishEvent, pairEventListeners, isSubPath, getServerWatchingFiles, getSelectedFromFileTree, getListItems, removeSelectedItemsFromList, removeDuplicatePaths, initListItems, initFileTree, getProjectWindow, showErrorDialog, isInPathList)
  lazy val joinProjectDialogFactory: JoinProjectDialog.Factory = () => new JoinProjectDialog(invokeLater, watchFilesDialogFactory, pairEventListeners, pluginLogger, publishEvent, showServerError, getExistingProjects, clientNameInGlobalStorage, getProjectWindow, getServerWatchingFiles)
  lazy val parseEvent = new ParseEvent
  lazy val clientFactory: Client.Factory = (serverAddress) => new Client(serverAddress)(parseEvent, pluginLogger)
  lazy val projectUrlHelper = new ProjectUrlHelper()
  lazy val copyToClipboard = new CopyToClipboard()
  lazy val projectUrlInProjectStorage = new ProjectUrlInProjectStorage(getCurrentProjectProperties)
  lazy val copyProjectUrlDialogFactory: CopyProjectUrlDialog.Factory = () => new CopyProjectUrlDialog(invokeLater, getProjectWindow, pairEventListeners, copyToClipboard, projectUrlInProjectStorage, pluginLogger)
  lazy val clientNameInCreationInProjectStorage = new ClientNameInCreationInProjectStorage(getCurrentProjectProperties)
  lazy val clientNameInJoinInProjectStorage = new ClientNameInJoinInProjectStorage(getCurrentProjectProperties)
  lazy val connectServerDialogFactory: ConnectServerDialog.Factory = () => new ConnectServerDialog(joinProjectDialogFactory, invokeLater, pairEventListeners, myChannelHandlerFactory, clientFactory, serverHostInProjectStorage, serverPortInProjectStorage, clientNameInCreationInProjectStorage, clientNameInJoinInProjectStorage, getProjectWindow, channelHandlerHolder, publishEvent, newUuid, projectUrlHelper, getServerWatchingFiles, watchFilesDialogFactory, copyProjectUrlDialogFactory, projectUrlInProjectStorage, setReadonlyMode, syncFilesForSlaveDialogFactory)
  lazy val inWatchingList = new InWatchingList(getServerWatchingFiles, isSubPath, getRelativePath)
  lazy val getUserData = new GetUserData
  lazy val putUserData = new PutUserData
  lazy val getCaretOffset = new GetCaretOffset
  lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(publishEvent, pluginLogger, inWatchingList, getDocumentContent, getUserData, putUserData, getRelativePath, getCaretOffset, isReadonlyMode)
  lazy val getSelectionEventInfo = new GetSelectionEventInfo
  lazy val projectSelectionListenerFactory = new ProjectSelectionListenerFactory(publishEvent, pluginLogger, inWatchingList, getRelativePath, getSelectionEventInfo, isReadonlyMode)
  lazy val projectDocumentListenerFactory = new ProjectDocumentListenerFactory(invokeLater, publishEvent, publishCreateDocumentEvent, newUuid, pluginLogger, clientVersionedDocuments, inWatchingList, getRelativePath, getDocumentContent, getCaretOffset, isReadonlyMode, getMyClientId)
  lazy val myFileEditorManagerFactory: MyFileEditorManager.Factory = () => new MyFileEditorManager(projectCaretListenerFactory, publishCreateDocumentEvent, projectDocumentListenerFactory, projectSelectionListenerFactory, pluginLogger, publishEvent, getRelativePath, tabEventsLocksInProject, isReadonlyMode)
  lazy val containsProjectFile = new ContainsProjectFile(getProjectBasePath, isSubPath)
  lazy val isWatching = new IsWatching(getServerWatchingFiles, isInPathList)
  lazy val myVirtualFileAdapterFactory: MyVirtualFileAdapter.Factory = () => new MyVirtualFileAdapter(invokeLater, publishEvent, pluginLogger, containsProjectFile, getRelativePath, getFileContent, getCachedFileContent, isWatching, isDirectory, clientVersionedDocuments, writeToProjectFile)
  lazy val clientIdToName = new ClientIdToName(getProjectInfoData)
  lazy val getAllClients = new GetAllClients(getProjectInfoData)
  lazy val getMasterClientId = new GetMasterClientId(getProjectInfoData)
  lazy val syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory = () => new SyncFilesForSlaveDialog(clientIdToName, watchFilesDialogFactory, invokeLater, pairEventListeners, getProjectWindow, getWatchingFileSummaries, connectionHolder, getMyClientId, getMasterClientId, getAllClients)
  lazy val getOtherClients = new GetOtherClients(getAllClients, getMyClientId)
  lazy val syncFilesForMasterDialogFactory: SyncFilesForMasterDialog.Factory = () => new SyncFilesForMasterDialog(connectionHolder, watchFilesDialogFactory, clientIdToName, invokeLater, pairEventListeners, getProjectWindow, getMyClientId, getOtherClients, getWatchingFileSummaries)
  lazy val amIMaster = new AmIMaster(clientInfoHolder)
  lazy val closeConnection = new CloseConnection(connectionHolder)
  lazy val copyProjectUrlToClipboard = new CopyProjectUrlToClipboard(projectUrlInProjectStorage, copyToClipboard)
  lazy val readonlyModeHolder = new ReadonlyModeHolder(notifyChanges, currentProjectScope)
  lazy val isReadonlyMode = new IsReadonlyMode(readonlyModeHolder)
  lazy val setReadonlyMode = new SetReadonlyMode(readonlyModeHolder)
  lazy val statusWidgetPopups = new StatusWidgetPopups(connectionHolder, invokeLater, publishEvent, localIp, syncFilesForMasterDialogFactory, syncFilesForSlaveDialogFactory, getAllClients, getProjectInfoData, isCaretSharing, serverHolder, showErrorDialog, amIMaster, closeConnection, copyProjectUrlToClipboard, isReadonlyMode, setReadonlyMode)
  lazy val createMessageConnection = new CreateMessageConnection(getMessageBus, currentProject)
  lazy val pairStatusWidgetFactory: PairStatusWidget.Factory = () => new PairStatusWidget(statusWidgetPopups, pluginLogger, serverHolder, amIMaster, createMessageConnection, isCaretSharing, connectionHolder, isReadonlyMode)
  lazy val getStatusBar = new GetStatusBar(currentProject)

}
