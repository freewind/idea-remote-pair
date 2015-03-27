package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{WatchingFiles, GetWatchingFilesFromPair, FileSummary}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleGetWatchingFilesFromPairSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleGetWatchingFilesFromPair = new HandleGetWatchingFilesFromPair(getMyClientId, publishEvent, getWatchingFileSummaries)

  getMyClientId.apply() returns Some("my-client-id")
  getWatchingFileSummaries.apply() returns Seq(FileSummary("/abc", "hello"))

  val event = new GetWatchingFilesFromPair("remote-client-id", "my-client-id")

  "When client receives GetWatchingFilesFromPair, it" should {
    "publish a WatchingFiles event" in {
      handleGetWatchingFilesFromPair(event)
      there was one(publishEvent).apply(new WatchingFiles("my-client-id", "remote-client-id", Seq(FileSummary("/abc", "hello"))))
    }
    "not publish event if can't get my client id" in {
      getMyClientId.apply() returns None
      handleGetWatchingFilesFromPair(event)
      there was no(publishEvent).apply(any)
    }
  }

}
