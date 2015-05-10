package com.thoughtworks.pli.remotepair.core.server_event_handlers.syncfiles

import com.thoughtworks.pli.remotepair.core.models.MyPlatform

class HandleSyncFilesForAll(myPlatform: MyPlatform, publishSyncFilesRequest: PublishSyncFilesRequest) {
  def apply(): Unit = myPlatform.invokeLater {
    publishSyncFilesRequest()
  }
}
