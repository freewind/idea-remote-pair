package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class CreateChildDirectory {
  def apply(file: IdeaFileImpl, newDirName: String): IdeaFileImpl = {
    file.createChildDirectory(newDirName)
  }
}
