package com.thoughtworks.pli.remotepair.idea.settings

import com.intellij.ide.util.PropertiesComponent

class GetIdeaProperties {
  def apply(): PropertiesComponent = PropertiesComponent.getInstance()

}


