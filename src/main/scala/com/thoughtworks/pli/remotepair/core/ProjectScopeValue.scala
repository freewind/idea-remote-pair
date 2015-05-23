package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyProject

case class ProjectScopeValue[T](currentProject: MyProject, key: String, initValue: T) {
  def set(value: T): T = currentProject.synchronized {
    currentProject.putUserData(key, value)
    currentProject.notifyUserDataChanges()
    value
  }
  def get: T = currentProject.synchronized {
    currentProject.getUserData[T](key) match {
      case null => set(initValue)
      case v => v
    }
  }
}
