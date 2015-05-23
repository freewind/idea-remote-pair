package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.remotepair.core.models.MyIde

class HandleSyncFilesForAll(myPlatform: MyIde, publishSyncFilesRequest: PublishSyncFilesRequest) {
  def apply(): Unit = myPlatform.invokeLater {
    publishSyncFilesRequest()
  }
}
