package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.core.client._

case class PublishSyncFilesRequest(myClient: MyClient) {

  def apply(watchingFiles: Seq[String] = myClient.serverWatchingFiles): Unit = myClient.myClientId match {
    case Some(clientId) => val files = myClient.watchingFileSummaries
      myClient.publishEvent(SyncFilesRequest(clientId, files))
    case None =>
  }

}
