package com.thoughtworks.pli.remotepair.idea.core.models.myfile

import com.intellij.openapi.vfs.VirtualFile

class FindChild {
  def apply(file: VirtualFile, name: String): Option[VirtualFile] = {
    Option(file.findChild(name))
  }
}
