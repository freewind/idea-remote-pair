package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.{PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.{HandleChangeContentConfirmation, HandleCreateDocumentConfirmation, HandleCreateServerDocumentRequest, HandleDocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.files._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.{HandleClientInfoResponse, HandleCreatedProjectEvent, HandleJoinedToProjectEvent, HandleServerStatusResponse}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles.{HandleSyncFileEvent, HandleSyncFilesForAll, HandleSyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.watching.{HandleGetWatchingFilesFromPair, HandleMasterWatchingFiles, HandleWatchFilesChangedEvent}

case class HandleEvent(handleOpenTabEvent: HandleOpenTabEvent,
                       handleCloseTabEvent: HandleCloseTabEvent,
                       myPlatform: MyPlatform,
                       publishCreateDocumentEvent: PublishCreateDocumentEvent,
                       publishEvent: PublishEvent,
                       handleChangeContentConfirmation: HandleChangeContentConfirmation,
                       handleMoveCaretEvent: HandleMoveCaretEvent,
                       highlightPairSelection: HighlightPairSelection,
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
                       logger: PluginLogger,
                       md5: Md5) {

  def apply(event: PairEvent): Unit = {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event)
      case event: CloseTabEvent => handleCloseTabEvent(event)
      case event: MoveCaretEvent => handleMoveCaretEvent(event)
      case event: SelectContentEvent => highlightPairSelection(event)
      case event: ServerErrorResponse => myPlatform.showErrorDialog("Get error message from server", event.message)
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
      case _ => logger.error("!!!! Can't handle: " + event)
    }
  }
}

