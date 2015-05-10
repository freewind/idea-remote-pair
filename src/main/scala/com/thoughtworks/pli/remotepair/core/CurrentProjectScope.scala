package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.intellij.openapi.util.Key

class CurrentProjectScope(currentProject: MyProject, synchronized: Synchronized) {
  def value[T](key: Key[T], creationValue: => T): ValueInCurrentProject[T] = new ValueInCurrentProject(key, creationValue)(currentProject, synchronized)
}
