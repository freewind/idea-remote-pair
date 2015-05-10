package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class GetFileChildren {
  def apply(dir: MyFile): Seq[MyFile] = dir.getChildren
}
