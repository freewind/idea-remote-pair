package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile

class GetFileName {
  def apply(file: VirtualFile): String = file.getName
}
