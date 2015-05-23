package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetWatchingFilesFromPair, WatchingFiles}
import com.thoughtworks.pli.remotepair.core.client.MyClient

case class HandleGetWatchingFilesFromPair(myClient: MyClient) {

  def apply(event: GetWatchingFilesFromPair): Unit = {
    for {
      myClientId <- myClient.myClientId
      fileSummaries = myClient.watchingFileSummaries
    } myClient.publishEvent(new WatchingFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
