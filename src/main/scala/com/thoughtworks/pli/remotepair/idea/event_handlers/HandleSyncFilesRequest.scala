package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, MasterWatchingFiles, SyncFileEvent, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs.AmIMaster

class HandleSyncFilesRequest(getAllWatchingFiles: GetAllWatchingFiles, publishEvent: PublishEvent, getMyClientId: GetMyClientId, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getFileSummary: GetFileSummary, amIMaster: AmIMaster) {

  def apply(req: SyncFilesRequest): Unit = if (amIMaster()) {
    val files = getAllWatchingFiles()
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    val myClientId = getMyClientId().get
    publishEvent(MasterWatchingFiles(myClientId, req.fromClientId, files.flatMap(f => getRelativePath(f)), diffs.length))
    for {
      file <- diffs
      path <- getRelativePath(file)
      content = getFileContent(file)
    } publishEvent(SyncFileEvent(myClientId, req.fromClientId, path, content))
  }

  private def calcDifferentFiles(localFiles: Seq[VirtualFile], fileSummaries: Seq[FileSummary]): Seq[VirtualFile] = {
    def isSameWithRemote(file: VirtualFile) = getFileSummary(file).exists(fileSummaries.contains)
    localFiles.filterNot(isSameWithRemote)
  }

}
