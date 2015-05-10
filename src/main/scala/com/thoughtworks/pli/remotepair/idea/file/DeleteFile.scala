package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile

class DeleteFile {
  def apply(file: MyFile) = file.delete()
}
