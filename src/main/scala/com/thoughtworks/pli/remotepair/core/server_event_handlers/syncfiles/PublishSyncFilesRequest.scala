package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.core.client.{PublishEvent, GetWatchingFileSummaries, GetServerWatchingFiles, GetMyClientId}

case class PublishSyncFilesRequest(getWatchingFileSummaries: GetWatchingFileSummaries, publishEvent: PublishEvent, getServerWatchingFiles: GetServerWatchingFiles, getMyClientId: GetMyClientId) {

  def apply(watchingFiles: Seq[String] = getServerWatchingFiles()): Unit = getMyClientId() match {
    case Some(clientId) => val files = getWatchingFileSummaries()
      publishEvent(SyncFilesRequest(clientId, files))
    case None =>
  }

}
