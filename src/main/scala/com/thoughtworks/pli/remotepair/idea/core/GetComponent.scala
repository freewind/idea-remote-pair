package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.project.Project

import scala.reflect.ClassTag

class GetComponent(currentProject: Project) {
  def apply[T: ClassTag](): T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    currentProject.getComponent(cls).asInstanceOf[T]
  }
}
