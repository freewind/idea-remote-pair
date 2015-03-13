package com.thoughtworks.pli.remotepair.idea.settings

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class GetCurrentProjectProperties(currentProject: RichProject) {
  def apply(): PropertiesComponent = PropertiesComponent.getInstance(currentProject.raw)
}
