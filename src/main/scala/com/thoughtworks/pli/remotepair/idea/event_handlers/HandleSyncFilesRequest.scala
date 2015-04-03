package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, MasterWatchingFiles, SyncFileEvent, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core._

class HandleSyncFilesRequest(getAllWatchingFiles: GetAllWatchingFiles, publishEvent: PublishEvent, getMyClientId: GetMyClientId, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getFileSummary: GetFileSummary) {

  def apply(req: SyncFilesRequest): Unit = {
    val files = getAllWatchingFiles()
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    val myClientId = getMyClientId().get
    publishEvent(MasterWatchingFiles(myClientId, req.fromClientId, files.flatMap(getRelativePath.apply), diffs.length))
    for {
      file <- diffs
      path <- getRelativePath(file)
      content = getFileContent(file)
    } publishEvent(SyncFileEvent(myClientId, req.fromClientId, path, content))
  }

  private def calcDifferentFiles(localFiles: Seq[VirtualFile], fileSummaries: Seq[FileSummary]): Seq[VirtualFile] = {
    def isSameWithRemote(file: VirtualFile) = fileSummaries.contains(getFileSummary(file))
    localFiles.filterNot(isSameWithRemote)
  }

}
