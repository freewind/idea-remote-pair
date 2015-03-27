package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleMasterWatchingFilesSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleMasterWatchingFiles = new HandleMasterWatchingFiles(getRelativePath, getAllWatchingFiles, invokeLater, runWriteAction, logger, deleteFile, fileExists, getFilePath)

  val event = new MasterWatchingFiles("remote-client-id", "my-client-id", Seq("/file1"), 0)
  val file1 = mock[VirtualFile]
  val file2 = mock[VirtualFile]

  getAllWatchingFiles.apply() returns Seq(file1, file2)
  getRelativePath.apply(file1) returns Some("/file1")
  getRelativePath.apply(file2) returns Some("/file2")
  fileExists.apply(any) returns true

  "When client receives MasterWatchingFiles, it" should {
    "delete self watching files which are not exist on master side" in {
      handleMasterWatchingFiles.apply(event)
      there was no(deleteFile).apply(file1)
      there was one(deleteFile).apply(file2)
    }

    "not delete self watching files if can't get relative path from file" in {
      getRelativePath.apply(any[VirtualFile]) returns None
      handleMasterWatchingFiles.apply(event)
      there was no(deleteFile).apply(any)
    }

    "not delete self watching files if they are all in event.paths" in {
      val event = new MasterWatchingFiles("remote-client-id", "my-client-id", Seq("/file1", "/file2"), 0)
      handleMasterWatchingFiles.apply(event)
      there was no(deleteFile).apply(any)
    }

    "not delete self watching files if they are not exist" in {
      fileExists.apply(any) returns false
      handleMasterWatchingFiles.apply(event)
      there was no(deleteFile).apply(any)
    }

  }

}
