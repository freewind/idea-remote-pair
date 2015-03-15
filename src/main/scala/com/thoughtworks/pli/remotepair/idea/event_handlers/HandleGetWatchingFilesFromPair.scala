package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetWatchingFilesFromPair, WatchingFiles}
import com.thoughtworks.pli.remotepair.idea.core.{GetWatchingFileSummaries, GetMyClientId, PublishEvent}

case class HandleGetWatchingFilesFromPair(getMyClientId: GetMyClientId, publishEvent: PublishEvent, getWatchingFileSummaries: GetWatchingFileSummaries) {

  def apply(event: GetWatchingFilesFromPair): Unit = {
    for {
      myClientId <- getMyClientId()
      fileSummaries = getWatchingFileSummaries()
    } publishEvent(new WatchingFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
