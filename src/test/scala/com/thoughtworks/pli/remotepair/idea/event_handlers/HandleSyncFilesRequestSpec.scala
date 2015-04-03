package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleSyncFilesRequestSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleSyncFilesRequest = new HandleSyncFilesRequest(getAllWatchingFiles, publishEvent, getMyClientId, getRelativePath, getFileContent, getFileSummary, amIMaster)

  val (file2, file3, file4) = (mock[VirtualFile], mock[VirtualFile], mock[VirtualFile])

  amIMaster.apply() returns true
  getMyClientId.apply() returns Some("my-id")

  getAllWatchingFiles.apply() returns Seq(file2, file3, file4)
  getRelativePath.apply(file2) returns Some("/file2")
  getFileSummary.apply(file2) returns Some(FileSummary("/file2", "summary2"))
  getFileContent.apply(file2) returns Content("content2", "UTF-8")
  getRelativePath.apply(file3) returns Some("/file3")
  getFileSummary.apply(file3) returns Some(FileSummary("/file3", "summary333333"))
  getFileContent.apply(file3) returns Content("content3", "UTF-8")
  getRelativePath.apply(file4) returns Some("/file4")
  getFileSummary.apply(file4) returns Some(FileSummary("/file4", "summary4"))
  getFileContent.apply(file4) returns Content("content4", "UTF-8")

  val request = new SyncFilesRequest("remote-client-id", Seq(FileSummary("/file1", "summary1"), FileSummary("/file2", "summary2"), FileSummary("/file3", "summary3")))

  "When client receives SyncFilesRequest, it" should {
    "publish a MasterWatchingFiles with all watching file paths and different file count to server" in {
      handleSyncFilesRequest(request)
      there was one(publishEvent).apply(MasterWatchingFiles("my-id", "remote-client-id", Seq("/file2", "/file3", "/file4"), 2))
    }
    "publish the SyncFileEvent for each different file" in {
      handleSyncFilesRequest(request)
      // TODO: http://stackoverflow.com/questions/29428031/how-to-verify-that-sender-has-only-sent-two-ints
      there was three(publishEvent).apply(any)
      there was one(publishEvent).apply(SyncFileEvent("my-id", "remote-client-id", "/file3", Content("content3", "UTF-8")))
      there was one(publishEvent).apply(SyncFileEvent("my-id", "remote-client-id", "/file4", Content("content4", "UTF-8")))
    }
    "do nothing is the client is not master" in {
      amIMaster.apply() returns false
      handleSyncFilesRequest(request)
      there was no(publishEvent).apply(any)
    }
  }

}
