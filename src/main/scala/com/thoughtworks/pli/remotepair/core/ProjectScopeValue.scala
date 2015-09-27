package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyProject}

case class ProjectScopeValue[T](currentProject: MyProject, key: DataKey[T], initValue: T) {
  def set(value: T): T = currentProject.synchronized {
    currentProject.putUserData(key, value)
    currentProject.notifyUserDataChanges()
    value
  }
  def get: T = currentProject.synchronized {
    currentProject.getUserData[T](key) match {
      case None => set(initValue)
      case Some(v) => v
    }
  }
}
