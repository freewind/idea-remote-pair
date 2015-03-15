package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

class GetServerIgnoredFiles(currentProject: RichProject) {
  def apply() = currentProject.projectInfo.toList.flatMap(_.ignoredFiles)
}
