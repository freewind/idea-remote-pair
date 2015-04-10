package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.{VirtualFile, VirtualFileEvent}
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class MyVirtualFileAdapterSpec extends Specification with Mockito with MocksModule {
  isolated

  val myVirtualFileAdapter = new MyVirtualFileAdapter(invokeLater, publishEvent, logger, containsProjectFile, getRelativePath, getFileContent, getCachedFileContent, isWatching, isDirectory)

  val file = mock[VirtualFile]
  getRelativePath(file) returns Some("/abc")
  containsProjectFile.apply(file) returns true
  isDirectory.apply(file) returns true
  isWatching.apply(file) returns true
  getFileContent(file) returns Content("helloworld", "UTF-8")

  val fileEvent = new VirtualFileEvent(null, file, "any-name", null)

  "When a dir is created, it" should {
    "send CreateDirEvent if it's in watching" in {
      isWatching.apply(file) returns true
      myVirtualFileAdapter.fileCreated(fileEvent)
      there was one(publishEvent).apply(CreateDirEvent("/abc"))
    }
    "not send CreateDirEvent if it's not in watching" in {
      isWatching.apply(file) returns false
      myVirtualFileAdapter.fileCreated(fileEvent)
      there was no(publishEvent).apply(any)
    }
  }

  "When a file is created, it" should {
    isDirectory.apply(file) returns false
    "send CreateFileEvent if it's in watching" in {
      isWatching.apply(file) returns true
      myVirtualFileAdapter.fileCreated(fileEvent)
      there was one(publishEvent).apply(CreateFileEvent("/abc", Content("helloworld", "UTF-8")))
    }
    "not send CreateFileEvent if it's not in watching" in {
      isWatching.apply(file) returns false
      myVirtualFileAdapter.fileCreated(fileEvent)
      there was no(publishEvent).apply(any)
    }
  }

  "When a dir is deleted, it" should {
    "send DeleteDirEvent if it's in watching" in {
      isWatching.apply(file) returns true
      myVirtualFileAdapter.fileDeleted(fileEvent)
      there was one(publishEvent).apply(DeleteDirEvent("/abc"))
    }
    "not send DeleteDirEvent if it's not in watching" in {
      isWatching.apply(file) returns false
      myVirtualFileAdapter.fileDeleted(fileEvent)
      there was no(publishEvent).apply(any)
    }
  }

  "When a file is deleted, it" should {
    isDirectory.apply(file) returns false
    "send DeleteFileEvent if it's in watching" in {
      isWatching.apply(file) returns true
      myVirtualFileAdapter.fileDeleted(fileEvent)
      there was one(publishEvent).apply(DeleteFileEvent("/abc"))
    }
    "not send DeleteFileEvent if it's not in watching" in {
      isWatching.apply(file) returns false
      myVirtualFileAdapter.fileDeleted(fileEvent)
      there was no(publishEvent).apply(any)
    }
  }

}
