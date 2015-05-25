package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.{MyEditor, MyFile, DataKey, MyProject}

class MemoryProject extends MyProject {
  override def putUserData[T](key: DataKey[T], value: T): Unit = ???
  override def baseDir: MyFile = ???
  override def showMessageDialog(message: String): Unit = ???
  override def openFileInTab(file: MyFile): Unit = ???
  override def findOrCreateDir(relativePath: String): MyFile = ???
  override def findOrCreateFile(relativePath: String): MyFile = ???
  override def showErrorDialog(title: String, message: String): Unit = ???
  override def openedFiles: Seq[MyFile] = ???
  override def notifyUserDataChanges(): Unit = ???
  override def getComponent[T](interfaceClass: Class[T]): T = ???
  override def getRelativePath(path: String): Option[String] = ???
  override def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = ???
  override def getFileByRelative(relativePath: String): Option[MyFile] = ???
  override def getUserData[T](key: DataKey[T]): T = ???
}
