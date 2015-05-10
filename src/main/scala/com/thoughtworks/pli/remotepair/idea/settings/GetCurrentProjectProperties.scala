package com.thoughtworks.pli.remotepair.idea.settings

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

case class GetCurrentProjectProperties(currentProject: IdeaProjectImpl) {
  def apply(): PropertiesComponent = PropertiesComponent.getInstance(currentProject.rawProject)
}
