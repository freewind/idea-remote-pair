package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HandleSyncFilesForAll(invokeLater: InvokeLater, publishSyncFilesRequest: PublishSyncFilesRequest) {
  def apply(): Unit = invokeLater {
    publishSyncFilesRequest()
  }
}
