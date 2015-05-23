package com.thoughtworks.pli.remotepair.core.models

trait MyProject {
  def putUserData[T](key: DataKey[T], value: T)
  def getUserData[T](key: DataKey[T]): T
  def getComponent[T](interfaceClass: Class[T]): T
  def baseDir: MyFile
  def openedFiles: Seq[MyFile]
  def findOrCreateDir(relativePath: String): MyFile
  def findOrCreateFile(relativePath: String): MyFile
  def getFileByRelative(relativePath: String): Option[MyFile]
  def getRelativePath(path: String): Option[String]
  def openFileInTab(file: MyFile): Unit
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor]
  def showErrorDialog(title: String, message: String): Unit
  def showMessageDialog(message: String): Unit

  def notifyUserDataChanges()
}
