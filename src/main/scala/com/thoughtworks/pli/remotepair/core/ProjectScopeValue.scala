package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyProject}

case class ProjectScopeValue[T](currentProject: MyProject, key: DataKey[T], initValue: T) {
  def set(value: T): T = {
    currentProject.putUserData(key, value, Some(() => currentProject.notifyUserDataChanges()))
    value
  }

  def get: T = {
    currentProject.getOrInitUserData(key, initValue)
  }
}
