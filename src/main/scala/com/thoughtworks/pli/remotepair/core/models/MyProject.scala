package com.thoughtworks.pli.remotepair.core.models

trait MyProject {
  def putUserData[T](key: String, value: T)
  def getUserData[T](key: String): T
  def getComponent[T](interfaceClass: Class[T]): T
  def baseDir: MyFile
  def openedFiles: Seq[MyFile]
  def findOrCreateDir(relativePath: String): MyFile
  def findOrCreateFile(relativePath: String): MyFile
  def getFileByRelative(relativePath: String): Option[MyFile]
  def getRelativePath(path: String): Option[String]
  def openFileInTab(file: MyFile): Unit
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor]
  def notifyUserDataChanges()
}
