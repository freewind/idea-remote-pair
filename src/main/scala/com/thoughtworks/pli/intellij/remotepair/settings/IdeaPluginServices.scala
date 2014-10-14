package com.thoughtworks.pli.intellij.remotepair.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

trait IdeaPluginServices {
  def appPropertiesService: PropertiesComponent = PropertiesComponent.getInstance()

  def projectPropertiesService(project: Project): PropertiesComponent = PropertiesComponent.getInstance(project)
}
