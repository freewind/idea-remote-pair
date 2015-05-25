package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, Content}
import com.thoughtworks.pli.remotepair.core.models.MyFile

class MemoryFile extends MyFile {
  override def exists: Boolean = ???
  override def move(newParent: MyFile): Unit = ???
  override def children: Seq[MyFile] = ???
  override def rename(newName: String): Unit = ???
  override def findChild(name: String): Option[MyFile] = ???
  override def setContent(newContent: String): Unit = ???
  override def cachedContent: Option[Content] = ???
  override def isActive: Boolean = ???
  override def summary: Option[FileSummary] = ???
  override def name: String = ???
  override def delete(): Unit = ???
  override def relativePath: Option[String] = ???
  override def isBinary: Boolean = ???
  override def isDirectory: Boolean = ???
  override def content: Content = ???
  override def createChildDirectory(name: String): MyFile = ???
  override def close(): Unit = ???
  override def isOpened: Boolean = ???
  override def path: String = ???
  override def isChildOf(parent: MyFile): Boolean = ???
  override def parent: MyFile = ???
}
