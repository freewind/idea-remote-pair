package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.idea.core.{GetWatchingFileSummaries, GetMyClientId, GetServerWatchingFiles, PublishEvent}

case class PublishSyncFilesRequest(getWatchingFileSummaries: GetWatchingFileSummaries, publishEvent: PublishEvent, getServerWatchingFiles: GetServerWatchingFiles, getMyClientId: GetMyClientId) {

  def apply(watchingFiles: Seq[String] = getServerWatchingFiles()): Unit = getMyClientId() match {
    case Some(clientId) => val files = getWatchingFileSummaries()
      publishEvent(SyncFilesRequest(clientId, files))
    case None =>
  }

}
