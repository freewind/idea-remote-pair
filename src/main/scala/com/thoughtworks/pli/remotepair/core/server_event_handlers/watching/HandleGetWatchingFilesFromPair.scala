package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetWatchingFilesFromPair, WatchingFiles}
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient

case class HandleGetWatchingFilesFromPair(connectedClient: ConnectedClient) {

  def apply(event: GetWatchingFilesFromPair): Unit = {
    for {
      myClientId <- connectedClient.myClientId
      fileSummaries = connectedClient.watchingFileSummaries
    } connectedClient.publishEvent(new WatchingFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
