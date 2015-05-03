package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.idea.core.{PluginLogger, PublishCreateDocumentEvent, PublishEvent, ShowServerError}
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleEvent(handleOpenTabEvent: HandleOpenTabEvent,
                       handleCloseTabEvent: HandleCloseTabEvent,
                       runWriteAction: RunWriteAction,
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
                       handleDocumentSnapshotEvent: HandleDocumentSnapshotEvent,
                       showServerError: ShowServerError,
                       invokeLater: InvokeLater,
                       logger: PluginLogger,
                       md5: Md5) {

  def apply(event: PairEvent): Unit = {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event)
      case event: CloseTabEvent => handleCloseTabEvent(event)
      case event: MoveCaretEvent => handleMoveCaretEvent(event)
      case event: SelectContentEvent => highlightPairSelection(event)
      case event: ServerErrorResponse => showServerError(event)
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
      case _ => logger.error("!!!! Can't handle: " + event)
    }
  }
}

