package com.thoughtworks.pli.intellij.remotepair.client

import com.intellij.openapi.project.Project

trait CurrentProjectHolder {
  def currentProject: Project
}
