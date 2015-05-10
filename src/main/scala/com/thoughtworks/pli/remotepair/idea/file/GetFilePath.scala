package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile

class GetFilePath {
  def apply(file: VirtualFile) = file.getPath
}
