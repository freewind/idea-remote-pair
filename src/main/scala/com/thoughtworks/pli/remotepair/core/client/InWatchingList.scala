package com.thoughtworks.pli.remotepair.core.client

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class InWatchingList(getServerWatchingFiles: GetServerWatchingFiles, isSubPath: IsSubPath, getRelativePath: GetRelativePath) {

  def apply(file: VirtualFile): Boolean = {
    getRelativePath(file).exists(path => getServerWatchingFiles().exists(isSubPath(path, _)))
  }

}
