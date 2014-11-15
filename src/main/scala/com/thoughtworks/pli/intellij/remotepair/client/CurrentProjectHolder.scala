package com.thoughtworks.pli.intellij.remotepair.client

import com.intellij.openapi.project.Project

trait CurrentProjectHolder {
  def currentProject: Project = CurrentProjectHolder.currentProject
  def currentProject_=(project: Project) = CurrentProjectHolder.currentProject = project
}

object CurrentProjectHolder {
  var currentProject: Project = null
}
