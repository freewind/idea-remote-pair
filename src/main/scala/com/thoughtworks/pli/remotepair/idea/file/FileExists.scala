package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class FileExists {
  def apply(file: MyFile) = file.exists()
}
