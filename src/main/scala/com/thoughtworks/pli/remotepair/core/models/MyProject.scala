package com.thoughtworks.pli.remotepair.core.models

import com.thoughtworks.pli.remotepair.core.models.MyProject.ProjectKey

trait MyProject {
  def putUserData[T](key: ProjectKey[T], value: T)
  def getUserData[T](key: ProjectKey[T]): T
  def getComponent[T](interfaceClass: Class[T]): T
  def getBaseDir: MyFile
  def getOpenedFiles: Seq[MyFile]
  def findOrCreateDir(relativePath: String): MyFile
  def findOrCreateFile(relativePath: String): MyFile
  def getFileByRelative(relativePath: String): Option[MyFile]
  def getRelativePath(path: String): Option[String]
  def openFileInTab(file: MyFile): Unit
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor]
}

object MyProject {
  case class ProjectKey[T](name: String)
}
