package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile

class IsDirectory {
  def apply(file: VirtualFile): Boolean = file.isDirectory
}
