package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.remotepair.core.models.MyIde

class HandleSyncFilesForAll(myIde: MyIde, publishSyncFilesRequest: PublishSyncFilesRequest) {
  def apply(): Unit = myIde.invokeLater {
    publishSyncFilesRequest()
  }
}
