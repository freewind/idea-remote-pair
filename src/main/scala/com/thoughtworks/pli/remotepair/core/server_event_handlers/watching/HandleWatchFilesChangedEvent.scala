package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesChangedEvent
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.idea.dialogs.SyncFilesForSlaveDialog

class HandleWatchFilesChangedEvent(myClient: MyClient, clientFactory: SyncFilesForSlaveDialog.Factory) {

  def apply(event: WatchFilesChangedEvent): Unit = {
    if (!myClient.amIMaster) {
      clientFactory().showOnCenter()
    }
  }

}
