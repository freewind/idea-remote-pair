package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile

class IsWatching(getServerWatchingFiles: GetServerWatchingFiles, isInPathList: IsInPathList) {
  def apply(file: VirtualFile): Boolean = {
    isInPathList.apply(file, getServerWatchingFiles())
  }
}
