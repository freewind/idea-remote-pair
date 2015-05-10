package com.thoughtworks.pli.remotepair.core.models

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.{VirtualFile, VirtualFileSystem}
import com.thoughtworks.pli.intellij.remotepair.protocol.Content

trait MyFile {
  def exists(): Boolean
  def isDirectory: Boolean
  def getFileType: FileType
  def findChild(name: String): Option[MyFile]
  def getName: String
  def getPath: String
  def content: Content
  def createChildDirectory(name: String): MyFile
  def delete(): Unit
  def getCharset: Charset
  def getCachedFileContent: Option[Content]
  def getChildren: Seq[MyFile]
  def getParent: MyFile
  def close(): Unit
  def move(newParent: MyFile): Unit
  def rename(newName: String): Unit
}

trait X extends VirtualFile {

  override def getLength: Long = ???
  override def getFileSystem: VirtualFileSystem = ???
  override def contentsToByteArray(): Array[Byte] = ???
  override def getParent: VirtualFile = ???
  override def refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable): Unit = ???
  override def getTimeStamp: Long = ???
  override def getOutputStream(requestor: scala.Any, newModificationStamp: Long, newTimeStamp: Long): OutputStream = ???
  override def isWritable: Boolean = ???
  override def isValid: Boolean = ???
  override def getChildren: Array[VirtualFile] = ???
  override def getInputStream: InputStream = ???
}
