package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.util.Key

trait MyProject {
  def putUserData[T](key: Key[T], value: T)
  def getUserData[T](key: Key[T]): T
  def getComponent[T](interfaceClass: Class[T]): T
  def getBaseDir: MyFile
  def getOpenedFiles(): Seq[MyFile]
  def findOrCreateDir(relativePath: String): MyFile
  def findOrCreateFile(relativePath: String): MyFile
  def getFileByRelative(relativePath: String): Option[MyFile]
  def getRelativePath(path: String): Option[String]
  def openFileInTab(file: MyFile): Unit
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor]
}

