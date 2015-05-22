package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.thoughtworks.pli.remotepair.core.models.MyProject.ProjectKey

case class ValueInCurrentProject[T](key: ProjectKey[T], creationValue: T)(currentProject: MyProject, synchronized: Synchronized) {
  def set(value: T): T = synchronized(currentProject) {
    currentProject.putUserData(key, value)
    value
  }
  def get: T = synchronized(currentProject) {
    currentProject.getUserData(key) match {
      case null => set(creationValue)
      case v => v
    }
  }
}
