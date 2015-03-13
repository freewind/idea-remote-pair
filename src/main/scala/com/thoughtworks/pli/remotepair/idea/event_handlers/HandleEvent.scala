package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.{PublishCreateDocumentEvent, PublishEvent, ShowErrorDialog}
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleEvent(currentProject: RichProject,
                  tabEventHandler: TabEventHandler,
                  runWriteAction: RunWriteAction,
                  publishCreateDocumentEvent: PublishCreateDocumentEvent,
                  publishEvent: PublishEvent,
                  handleChangeContentConfirmation: HandleChangeContentConfirmation,
                  publishSyncFilesRequest: PublishSyncFilesRequest,
                  handleResetTabRequest: HandleResetTabRequest,
                  moveCaret: MoveCaret,
                  highlightPairSelection: HighlightPairSelection,
                  handleSyncFilesRequest: HandleSyncFilesRequest,
                  handleMasterPairableFiles: HandleMasterPairableFiles,
                  handleCreateServerDocumentRequest: HandleCreateServerDocumentRequest,
                  handleCreateDocumentConfirmation: HandleCreateDocumentConfirmation,
                  handleGetPairableFilesFromPair: HandleGetPairableFilesFromPair,
                  handleJoinedToProjectEvent: HandleJoinedToProjectEvent,
                  handleServerStatusResponse: HandleServerStatusResponse,
                  handleClientInfoResponse: HandleClientInfoResponse,
                  handleSyncFilesForAll: HandleSyncFilesForAll,
                  handleSyncFileEvent: HandleSyncFileEvent,
                  handleCreateDirEvent: HandleCreateDirEvent,
                  handleDeleteFileEvent: HandleDeleteFileEvent,
                  handleDeleteDirEvent: HandleDeleteDirEvent,
                  handleCreateFileEvent: HandleCreateFileEvent,
                  showErrorDialog: ShowErrorDialog,
                  invokeLater: InvokeLater,
                  logger: Logger,
                  md5: Md5) {

  def apply(event: PairEvent): Unit = {
    event match {
      case event: OpenTabEvent => tabEventHandler.handleOpenTabEvent(event.path)
      case event: CloseTabEvent => tabEventHandler.handleCloseTabEvent(event.path)
      case event: ResetTabEvent => tabEventHandler.handleOpenTabEvent(event.path)
      case ResetTabRequest => handleResetTabRequest()
      case event: MoveCaretEvent => moveCaret(event.path, event.offset)
      case event: SelectContentEvent => highlightPairSelection(event)
      case event: ServerErrorResponse => showErrorDialog(event)
      case event: ServerStatusResponse => handleServerStatusResponse(event)
      case event: ClientInfoResponse => handleClientInfoResponse(event)
      case req: SyncFilesRequest => handleSyncFilesRequest(req)
      case SyncFilesForAll => handleSyncFilesForAll()
      case event: MasterPairableFiles => handleMasterPairableFiles(event)
      case event: SyncFileEvent => handleSyncFileEvent(event)
      case event: CreateDirEvent => handleCreateDirEvent(event)
      case event: CreateFileEvent => handleCreateFileEvent(event)
      case event: DeleteFileEvent => handleDeleteFileEvent(event)
      case event: DeleteDirEvent => handleDeleteDirEvent(event)
      case event: ChangeContentConfirmation => handleChangeContentConfirmation(event)
      case request: CreateServerDocumentRequest => handleCreateServerDocumentRequest(request)
      case event: CreateDocumentConfirmation => handleCreateDocumentConfirmation(event)
      case event: PairableFiles => ()
      case event: GetPairableFilesFromPair => handleGetPairableFilesFromPair(event)
      case event: ProjectOperationFailed => ()
      case event: JoinedToProjectEvent => handleJoinedToProjectEvent(event)
      case _ => logger.error("!!!! Can't handle: " + event)
    }
  }
}

