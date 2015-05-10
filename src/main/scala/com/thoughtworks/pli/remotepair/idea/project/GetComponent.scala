package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.models.MyProject

import scala.reflect.ClassTag

class GetComponent(currentProject: MyProject) {
  def apply[T: ClassTag](): T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    currentProject.getComponent(cls).asInstanceOf[T]
  }
}
