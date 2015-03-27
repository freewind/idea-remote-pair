package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class DeleteFile {
  def apply(file: VirtualFile) = file.delete(this)
}
