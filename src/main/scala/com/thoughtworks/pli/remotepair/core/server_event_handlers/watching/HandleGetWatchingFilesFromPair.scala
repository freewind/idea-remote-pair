package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetWatchingFilesFromPair, WatchingFiles}
import com.thoughtworks.pli.remotepair.core.client.{PublishEvent, GetWatchingFileSummaries, GetMyClientId}

case class HandleGetWatchingFilesFromPair(getMyClientId: GetMyClientId, publishEvent: PublishEvent, getWatchingFileSummaries: GetWatchingFileSummaries) {

  def apply(event: GetWatchingFilesFromPair): Unit = {
    for {
      myClientId <- getMyClientId()
      fileSummaries = getWatchingFileSummaries()
    } publishEvent(new WatchingFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
