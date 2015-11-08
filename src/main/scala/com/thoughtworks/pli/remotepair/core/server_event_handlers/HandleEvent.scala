package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.{MyUtils, PluginLogger}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles, HandleWatchFilesChangedEvent}

case class HandleEvent(handleOpenTabEvent: HandleOpenTabEvent,
                       handleCloseTabEvent: HandleCloseTabEvent,
                       myClient: MyClient,
                       currentProject: MyProject,
                       myUtils: MyUtils,
                       handleChangeContentConfirmation: HandleChangeContentConfirmation,
                       pairCarets: PairCarets,
                       pairSelections: PairSelections,
                       handleSyncFilesRequest: HandleSyncFilesRequest,
                       handleMasterWatchingFiles: HandleMasterWatchingFiles,
                       handleCreateServerDocumentRequest: HandleCreateServerDocumentRequest,
                       handleCreateDocumentConfirmation: HandleCreateDocumentConfirmation,
                       handleGetPairableFilesFromPair: HandleGetWatchingFilesFromPair,
                       handleJoinedToProjectEvent: HandleJoinedToProjectEvent,
                       handleCreatedProjectEvent: HandleCreatedProjectEvent,
                       handleServerStatusResponse: HandleServerStatusResponse,
                       handleClientInfoResponse: HandleClientInfoResponse,
                       handleSyncFilesForAll: HandleSyncFilesForAll,
                       handleSyncFileEvent: HandleSyncFileEvent,
                       handleCreateDirEvent: HandleCreateDirEvent,
                       handleDeleteFileEvent: HandleDeleteFileEvent,
                       handleDeleteDirEvent: HandleDeleteDirEvent,
                       handleCreateFileEvent: HandleCreateFileEvent,
                       handleRenameDirEvent: HandleRenameDirEvent,
                       handleRenameFileEvent: HandleRenameFileEvent,
                       handleMoveDirEvent: HandleMoveDirEvent,
                       handleMoveFileEvent: HandleMoveFileEvent,
                       handleDocumentSnapshotEvent: HandleDocumentSnapshotEvent,
                       handleWatchFilesChangedEvent: HandleWatchFilesChangedEvent,
                       logger: PluginLogger) {

  def apply(event: PairEvent): Unit = {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event)
      case event: CloseTabEvent => handleCloseTabEvent(event)
      case event: MoveCaretEvent => pairCarets.draw(event)
      case event: SelectContentEvent => pairSelections.highlight(event)
      case event: ServerErrorResponse => currentProject.showErrorDialog("Get error message from server", event.message)
      case event: ServerStatusResponse => handleServerStatusResponse(event)
      case event: ClientInfoResponse => handleClientInfoResponse(event)
      case req: SyncFilesRequest => handleSyncFilesRequest(req)
      case SyncFilesForAll => handleSyncFilesForAll()
      case event: MasterWatchingFiles => handleMasterWatchingFiles(event)
      case event: SyncFileEvent => handleSyncFileEvent(event)
      case event: CreateDirEvent => handleCreateDirEvent(event)
      case event: CreateFileEvent => handleCreateFileEvent(event)
      case event: DeleteFileEvent => handleDeleteFileEvent(event)
      case event: DeleteDirEvent => handleDeleteDirEvent(event)
      case event: ChangeContentConfirmation => handleChangeContentConfirmation(event)
      case request: CreateServerDocumentRequest => handleCreateServerDocumentRequest(request)
      case event: CreateDocumentConfirmation => handleCreateDocumentConfirmation(event)
      case event: WatchingFiles => ()
      case event: GetWatchingFilesFromPair => handleGetPairableFilesFromPair(event)
      case event: ProjectOperationFailed => ()
      case event: JoinedToProjectEvent => handleJoinedToProjectEvent(event)
      case event: CreatedProjectEvent => handleCreatedProjectEvent(event)
      case event: DocumentSnapshotEvent => handleDocumentSnapshotEvent(event)
      case event: RenameDirEvent => handleRenameDirEvent(event)
      case event: RenameFileEvent => handleRenameFileEvent(event)
      case event: MoveDirEvent => handleMoveDirEvent(event)
      case event: MoveFileEvent => handleMoveFileEvent(event)
      case event: WatchFilesChangedEvent => handleWatchFilesChangedEvent(event)
      case event: ServerVersionInfo => // TODO
      case _ => logger.error("!!!! Can't handle: " + event)
    }
  }
}

