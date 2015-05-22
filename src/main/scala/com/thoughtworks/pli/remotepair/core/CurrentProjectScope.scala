package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyProject
import com.thoughtworks.pli.remotepair.core.models.MyProject.ProjectKey

class CurrentProjectScope(currentProject: MyProject, synchronized: Synchronized) {
  def value[T](key: ProjectKey[T], creationValue: => T): ValueInCurrentProject[T] = new ValueInCurrentProject(key, creationValue)(currentProject, synchronized)
}
