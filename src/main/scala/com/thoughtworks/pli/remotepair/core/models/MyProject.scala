package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.util.Key

trait MyProject {
  def putUserData[T](key: Key[T], value: T)
  def getUserData[T](key: Key[T]): T
  def getComponent[T](interfaceClass: Class[T]): T
  def getBaseDir: MyFile
}

