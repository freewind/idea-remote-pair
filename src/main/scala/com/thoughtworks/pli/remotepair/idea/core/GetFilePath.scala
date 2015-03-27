package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class GetFilePath {
  def apply(file: VirtualFile) = file.getPath
}
