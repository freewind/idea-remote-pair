package com.thoughtworks.pli.remotepair.idea.models

import java.nio.charset.Charset

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import com.thoughtworks.pli.remotepair.core.models.MyFile

class IdeaFileImpl(val raw: VirtualFile) extends MyFile {
  override def exists(): Boolean = ???
  override def getCharset: Charset = ???
  override def getCachedFileContent: Option[Content] = ???
  override def getFileType: FileType = ???
  override def getName: String = ???
  override def findChild(name: String): Option[IdeaFileImpl] = ???
  override def delete(): Unit = ???
  override def isDirectory: Boolean = ???
  override def content: Content = ???
  override def getPath: String = ???
  override def createChildDirectory(name: String): IdeaFileImpl = ???
  override def getChildren: Seq[MyFile] = ???
  override def getParent: MyFile = ???
  override def move(newParent: MyFile): Unit = ???
  override def rename(newName: String): Unit = ???
  override def close(): Unit = ???
}

object IdeaFileImpl {
  def apply(file: VirtualFile) = new IdeaFileImpl(file)
}
