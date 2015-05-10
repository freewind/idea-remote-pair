package com.thoughtworks.pli.remotepair.idea.project

import com.thoughtworks.pli.remotepair.idea.models.{IdeaFileImpl, IdeaProjectImpl}

class GetProjectBaseDir(currentProject: IdeaProjectImpl) {
  def apply(): IdeaFileImpl = currentProject.getBaseDir
}
