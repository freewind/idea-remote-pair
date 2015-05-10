package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class FindChild {
  def apply(file: IdeaFileImpl, name: String): Option[IdeaFileImpl] = {
    file.findChild(name)
  }
}
