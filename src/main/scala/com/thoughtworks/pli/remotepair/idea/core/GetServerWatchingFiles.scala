package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

class GetServerWatchingFiles(currentProject: RichProject) {
  def apply() = currentProject.projectInfo.toList.flatMap(_.watchingFiles)
}
