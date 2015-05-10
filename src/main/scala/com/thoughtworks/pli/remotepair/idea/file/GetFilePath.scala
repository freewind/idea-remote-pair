package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class GetFilePath {
  def apply(file: MyFile) = file.getPath
}
