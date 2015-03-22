package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class CurrentProjectScope(currentProject: Project, synchronized: Synchronized) {
  def value[T](key: Key[T], creationValue: => T) = new ValueInCurrentProject(key, creationValue)(currentProject, synchronized)
}
