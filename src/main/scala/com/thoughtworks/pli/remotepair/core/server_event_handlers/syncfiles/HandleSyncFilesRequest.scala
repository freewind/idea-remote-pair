package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, MasterWatchingFiles, SyncFileEvent, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyFile

class HandleSyncFilesRequest(myClient: MyClient) {

  def apply(req: SyncFilesRequest): Unit = if (myClient.amIMaster) {
    val files = myClient.allWatchingFiles
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    val myClientId = myClient.myClientId.get
    myClient.publishEvent(MasterWatchingFiles(myClientId, req.fromClientId, files.flatMap(_.relativePath), diffs.length))
    for {
      file <- diffs
      path <- file.relativePath
      content = file.content
    } myClient.publishEvent(SyncFileEvent(myClientId, req.fromClientId, path, content))
  }

  private def calcDifferentFiles(localFiles: Seq[MyFile], fileSummaries: Seq[FileSummary]): Seq[MyFile] = {
    def isSameWithRemote(file: MyFile) = file.summary.exists(fileSummaries.contains)
    localFiles.filterNot(isSameWithRemote)
  }

}
