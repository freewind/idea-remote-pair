package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.StandardizePath

class GetProjectBasePath(getProjectBaseDir: GetProjectBaseDir, standardizePath: StandardizePath) {
  def apply(): String = standardizePath(getProjectBaseDir().getPath)
}
