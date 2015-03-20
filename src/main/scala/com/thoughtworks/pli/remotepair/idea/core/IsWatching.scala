package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class IsWatching(getRelativePath: GetRelativePath) {
  def apply(file: VirtualFile, watchingPaths: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    watchingPaths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }
}
