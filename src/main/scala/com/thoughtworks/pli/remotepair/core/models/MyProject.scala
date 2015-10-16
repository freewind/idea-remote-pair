package com.thoughtworks.pli.remotepair.core.models

trait MyProject {
  def putUserData[T](key: DataKey[T], value: T, postAction: Option[() => Unit] = None): Unit
  def getUserData[T](key: DataKey[T]): Option[T]
  def getOrInitUserData[T](key: DataKey[T], initValue: T): T
  def baseDir: MyFile
  def openedFiles: Seq[MyFile]
  def findOrCreateDir(relativePath: String): MyFile
  def findOrCreateFile(relativePath: String): MyFile
  def getFileByRelative(relativePath: String): Option[MyFile]
  def getRelativePath(path: String): Option[String]
  def openFileInTab(file: MyFile): Unit
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor]
  def showErrorDialog(title: String = "Error", message: String): Unit
  def showMessageDialog(message: String): Unit

  def close(file: MyFile): Unit
  def isOpened(file: MyFile): Boolean
  def isActive(file: MyFile): Boolean

  def notifyUserDataChanges()
}
