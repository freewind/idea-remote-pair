package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class CurrentProjectScope(currentProject: Project, synchronized: Synchronized) {
  def value[T](key: Key[T], creationValue: => T): ValueInCurrentProject[T] = new ValueInCurrentProject(key, creationValue)(currentProject, synchronized)
}
