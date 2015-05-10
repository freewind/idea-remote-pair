package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.core.models.MyProject

class IdeaProjectImpl(val raw: Project) extends MyProject {
  override def putUserData[T](key: Key[T], value: T): Unit = raw.putUserData(key, value)
  override def getUserData[T](key: Key[T]): T = raw.getUserData(key)
  override def getBaseDir: IdeaFileImpl = new IdeaFileImpl(raw.getBaseDir)
  override def getComponent[T](interfaceClass: Class[T]): T = raw.getComponent(interfaceClass)
}
