package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.vfs._

object MyVirtualFileAdapter extends VirtualFileAdapter {
  override def fileDeleted(event: VirtualFileEvent) {
    println("### file deleted: " + event.getFile)
  }

  override def fileCreated(event: VirtualFileEvent) {
    println("### file created: " + event.getFile)
  }

  override def fileMoved(event: VirtualFileMoveEvent) {
    println("### file moved: " + event.getFile)
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) {
    println("### file property changed: " + event.getFile)
  }

  override def fileCopied(event: VirtualFileCopyEvent) {
    println("### file copied: " + event.getFile)
  }

  override def contentsChanged(event: VirtualFileEvent) {
    println("### contents changed: " + event.getFile)
  }

}
