package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class IsInPathList(getRelativePath: GetRelativePath) {
  def apply(file: VirtualFile, paths: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }
}
