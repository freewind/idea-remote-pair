package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.core.RuntimeAssertions
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class GetFileByRelative(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir) {

  import runtimeAssertions.goodPath

  def apply(path: String): Option[IdeaFileImpl] = {
    assume(goodPath(path))
    Option(new IdeaFileImpl(getProjectBaseDir().raw.findFileByRelativePath(path)))
  }

}
