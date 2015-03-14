package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.softwaremill.macwire.Macwire
import com.thoughtworks.pli.intellij.remotepair.protocol.ParseEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{IsSubPath, Md5, NewUuid}
import com.thoughtworks.pli.remotepair.idea.actions.StartServer
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.event_handlers._
import com.thoughtworks.pli.remotepair.idea.listeners.{ProjectCaretListener, ProjectDocumentListener, ProjectSelectionListener}
import com.thoughtworks.pli.remotepair.idea.settings.{GetCurrentProjectProperties, GetIdeaProperties, IdeaSettingsProperties, ProjectSettingsProperties}
import com.thoughtworks.pli.remotepair.idea.statusbar.{PairStatusWidgetFactory, StatusWidgetPopups}
import com.thoughtworks.pli.remotepair.idea.utils._
import org.specs2.mock.Mockito

trait MocksModule extends Macwire {
  this: Mockito =>

  lazy val logger = mock[Logger]

  lazy val newUuid = mock[NewUuid]

  lazy val runtimeAssertions = mock[RuntimeAssertions]
  lazy val md5 = mock[Md5]
  lazy val isSubPath = mock[IsSubPath]
  lazy val localHostName = mock[GetLocalHostName]
  lazy val localIp = mock[GetLocalIp]
  lazy val invokeLater = mock[InvokeLater]
  lazy val runReadAction = mock[RunReadAction]
  lazy val richProjectFactory = mock[RichProjectFactory]
  lazy val getIdeaProperties = mock[GetIdeaProperties]
  lazy val ideaSettingsProperties = mock[IdeaSettingsProperties]
  lazy val rawProject = mock[Project]

  lazy val currentProject: RichProject = richProjectFactory.create(rawProject)

  lazy val publishEvent = mock[PublishEvent]
  lazy val runWriteAction = mock[RunWriteAction]
  lazy val pairEventListeners = mock[PairEventListeners]
  lazy val getCurrentProjectProperties = mock[GetCurrentProjectProperties]
  lazy val projectSettingsProperties = mock[ProjectSettingsProperties]
  lazy val startServer = mock[StartServer]

  // event handlers
  lazy val publishSyncFilesRequest = mock[PublishSyncFilesRequest]
  lazy val tabEventHandler = mock[TabEventHandler]
  lazy val publishCreateDocumentEvent = mock[PublishCreateDocumentEvent]
  lazy val newHighlights = mock[NewHighlights]
  lazy val removeOldHighlighters = mock[RemoveOldHighlighters]
  lazy val clientVersionedDocumentFactory = mock[ClientVersionedDocumentFactory]
  lazy val clientVersionedDocuments = mock[ClientVersionedDocuments]
  lazy val handleChangeContentConfirmation = mock[HandleChangeContentConfirmation]
  lazy val handleResetTabRequest = mock[HandleResetTabRequest]
  lazy val moveCaret = mock[MoveCaret]
  lazy val handleCreateServerDocumentRequest = mock[HandleCreateServerDocumentRequest]
  lazy val highlightPairSelection = mock[HighlightPairSelection]
  lazy val handleSyncFilesRequest = mock[HandleSyncFilesRequest]
  lazy val handleMasterPairableFiles = mock[HandleMasterPairableFiles]
  lazy val handleCreateDocumentConfirmation = mock[HandleCreateDocumentConfirmation]
  lazy val handleGetPairableFilesFromPair = mock[HandleGetPairableFilesFromPair]
  lazy val handleServerStatusResponse = mock[HandleServerStatusResponse]
  lazy val handleJoinedToProjectEvent = mock[HandleJoinedToProjectEvent]
  lazy val handleSyncFilesForAll = mock[HandleSyncFilesForAll]
  lazy val handleSyncFileEvent = mock[HandleSyncFileEvent]
  lazy val handleCreateDirEvent = mock[HandleCreateDirEvent]
  lazy val handleDeleteFileEvent = mock[HandleDeleteFileEvent]
  lazy val handleClientInfoResponse = mock[HandleClientInfoResponse]
  lazy val handleDeleteDirEvent = mock[HandleDeleteDirEvent]
  lazy val showErrorDialog = mock[ShowErrorDialog]
  lazy val handleCreateFileEvent = mock[HandleCreateFileEvent]
  lazy val handleEvent = mock[HandleEvent]

  lazy val connectionFactory = mock[ConnectionFactory]
  lazy val myChannelHandlerFactory = mock[MyChannelHandlerFactory]

  lazy val chooseIgnoreDialogFactory = mock[ChooseIgnoreDialogFactory]
  lazy val joinProjectDialogFactory = mock[JoinProjectDialogFactory]
  lazy val parseEvent = mock[ParseEvent]
  lazy val clientFactory = mock[ClientFactory]
  lazy val connectServerDialogFactory = mock[ConnectServerDialogFactory]

  lazy val projectCaretListener = mock[ProjectCaretListener]
  lazy val projectSelectionListener = mock[ProjectSelectionListener]
  lazy val projectDocumentListener = mock[ProjectDocumentListener]
  lazy val myFileEditorManagerFactory = mock[MyFileEditorManagerFactory]
  lazy val myVirtualFileAdapterFactory = mock[MyVirtualFileAdapterFactory]
  lazy val clientName = mock[ClientName]
  lazy val syncFilesForSlaveDialogFactory = mock[SyncFilesForSlaveDialogFactory]
  lazy val syncFilesForMasterDialogFactory = mock[SyncFilesForMasterDialogFactory]
  lazy val statusWidgetPopups = mock[StatusWidgetPopups]
  lazy val pairStatusWidgetFactory = mock[PairStatusWidgetFactory]
}
