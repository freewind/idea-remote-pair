package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath

class InWatchingList(getServerWatchingFiles: GetServerWatchingFiles, isSubPath: IsSubPath, getRelativePath: GetRelativePath) {

  def apply(file: VirtualFile): Boolean = {
    getRelativePath(file).exists(path => getServerWatchingFiles().exists(isSubPath(path, _)))
  }

}
