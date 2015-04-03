package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleSyncFilesForAllSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleSyncFilesForAll = new HandleSyncFilesForAll(invokeLater, publishSyncFilesRequest)

  "When client receives SyncFilesForAll, it" should {
    "publish SyncFilesRequest to server" in {
      handleSyncFilesForAll()
      there was one(publishSyncFilesRequest).apply()
    }
  }

}
