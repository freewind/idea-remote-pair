package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import org.jetbrains.annotations.NotNull
import com.intellij.util.messages.MessageBusConnection
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.util.{UserDataHolder, Key}
import com.intellij.openapi.editor.Editor

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
