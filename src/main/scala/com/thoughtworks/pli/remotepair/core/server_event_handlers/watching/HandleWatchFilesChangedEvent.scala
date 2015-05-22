package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesChangedEvent
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.idea.dialogs.SyncFilesForSlaveDialog

class HandleWatchFilesChangedEvent(connectedClient: ConnectedClient, clientFactory: SyncFilesForSlaveDialog.Factory) {

  def apply(event: WatchFilesChangedEvent): Unit = {
    if (!connectedClient.amIMaster) {
      clientFactory().showOnCenter()
    }
  }

}
