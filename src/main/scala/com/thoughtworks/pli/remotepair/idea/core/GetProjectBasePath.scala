package com.thoughtworks.pli.remotepair.idea.core

class GetProjectBasePath(getProjectBaseDir: GetProjectBaseDir, standardizePath: StandardizePath) {
  def apply(): String = standardizePath(getProjectBaseDir().getPath)
}
