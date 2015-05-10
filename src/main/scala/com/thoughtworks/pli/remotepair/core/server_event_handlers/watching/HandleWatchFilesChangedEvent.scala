package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesChangedEvent
import com.thoughtworks.pli.remotepair.core.client.AmIMaster
import com.thoughtworks.pli.remotepair.idea.dialogs.SyncFilesForSlaveDialog

class HandleWatchFilesChangedEvent(amIMaster: AmIMaster, clientFactory: SyncFilesForSlaveDialog.Factory) {

  def apply(event: WatchFilesChangedEvent): Unit = {
    if (!amIMaster()) {
      clientFactory().showOnCenter()
    }
  }

}
