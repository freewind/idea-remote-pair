package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile

class FileExists {
  def apply(file: VirtualFile) = file.exists()
}
