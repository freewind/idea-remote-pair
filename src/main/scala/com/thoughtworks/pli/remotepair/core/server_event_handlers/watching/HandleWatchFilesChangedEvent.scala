package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesChangedEvent
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories

class HandleWatchFilesChangedEvent(myClient: MyClient, dialogFactories: DialogFactories) {

  def apply(event: WatchFilesChangedEvent): Unit = {
    if (!myClient.amIMaster) {
      dialogFactories.createSyncFilesForSlaveDialog.showOnCenter()
    }
  }

}
