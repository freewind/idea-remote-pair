package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HandleSyncFilesForAll(invokeLater: InvokeLater, publishSyncFilesRequest: PublishSyncFilesRequest) {
  def apply(): Unit = invokeLater {
    publishSyncFilesRequest()
  }
}
