package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.core.client._

case class PublishSyncFilesRequest(connectedClient: ConnectedClient) {

  def apply(watchingFiles: Seq[String] = connectedClient.serverWatchingFiles): Unit = connectedClient.myClientId match {
    case Some(clientId) => val files = connectedClient.watchingFileSummaries
      connectedClient.publishEvent(SyncFilesRequest(clientId, files))
    case None =>
  }

}
