package com.thoughtworks.pli.remotepair.core.client

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.IsInPathList

class IsWatching(getServerWatchingFiles: GetServerWatchingFiles, isInPathList: IsInPathList) {
  def apply(file: VirtualFile): Boolean = {
    isInPathList.apply(file, getServerWatchingFiles())
  }
}
