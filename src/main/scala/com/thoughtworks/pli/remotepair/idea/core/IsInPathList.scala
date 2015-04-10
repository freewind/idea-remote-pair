package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class IsInPathList(getRelativePath: GetRelativePath) {
  def apply(file: VirtualFile, paths: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }
}
