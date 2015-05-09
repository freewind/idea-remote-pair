package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class CreateChildDirectory {
  def apply(file: VirtualFile, newDirName: String): VirtualFile = {
    file.createChildDirectory(this, newDirName)
  }
}
