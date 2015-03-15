package com.thoughtworks.pli.remotepair.idea.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

case class GetCurrentProjectProperties(currentProject: Project) {
  def apply(): PropertiesComponent = PropertiesComponent.getInstance(currentProject)
}
