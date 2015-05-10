package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class IsDirectory {
  def apply(file: MyFile): Boolean = file.isDirectory
}
