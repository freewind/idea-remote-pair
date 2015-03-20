package com.thoughtworks.pli.remotepair.idea.core.files

import com.intellij.openapi.vfs.VirtualFile

class GetFileChildren {
  def apply(dir: VirtualFile): Seq[VirtualFile] = dir.getChildren.toSeq
}
