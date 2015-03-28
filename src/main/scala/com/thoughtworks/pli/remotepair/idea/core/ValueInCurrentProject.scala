package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

case class ValueInCurrentProject[T](key: Key[T], creationValue: T)(currentProject: Project, synchronized: Synchronized) {
  def set(value: T): T = synchronized(currentProject) {
    currentProject.putUserData(key, value)
    value
  }
  def get = synchronized(currentProject) {
    currentProject.getUserData(key) match {
      case null => set(creationValue)
      case v => v
    }
  }
}
