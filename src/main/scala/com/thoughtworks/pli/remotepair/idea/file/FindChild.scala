package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile

class FindChild {
  def apply(file: VirtualFile, name: String): Option[VirtualFile] = {
    Option(file.findChild(name))
  }
}
