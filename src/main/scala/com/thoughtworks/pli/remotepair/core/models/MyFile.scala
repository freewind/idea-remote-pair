package com.thoughtworks.pli.remotepair.core.models

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, FileSummary}

trait MyFile {
  def exists: Boolean
  def isDirectory: Boolean
  def isBinary: Boolean
  def findChild(name: String): Option[MyFile]
  def name: String
  def path: String
  def content: Content
  def setContent(newContent: String): Unit
  def summary: Option[FileSummary]
  def cachedContent: Option[Content]
  def createChildDirectory(name: String): MyFile
  def delete(): Unit
  def children: Seq[MyFile]
  def parent: MyFile
  def close(): Unit
  def move(newParent: MyFile): Unit
  def rename(newName: String): Unit
  def relativePath: Option[String]
  def isChildOf(parent: MyFile): Boolean
  def isOpened: Boolean
  def isActive: Boolean
}
