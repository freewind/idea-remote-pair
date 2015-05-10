package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.intellij.openapi.util.Key

case class ValueInCurrentProject[T](key: Key[T], creationValue: T)(currentProject: MyProject, synchronized: Synchronized) {
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
