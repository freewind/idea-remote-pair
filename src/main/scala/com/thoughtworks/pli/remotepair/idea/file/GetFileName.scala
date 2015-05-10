package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class GetFileName {
  def apply(file: MyFile): String = file.getName
}
