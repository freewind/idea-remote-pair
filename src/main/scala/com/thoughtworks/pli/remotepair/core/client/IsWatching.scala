package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core.IsInPathList

class IsWatching(getServerWatchingFiles: GetServerWatchingFiles, isInPathList: IsInPathList) {
  def apply(file: MyFile): Boolean = {
    isInPathList.apply(file, getServerWatchingFiles())
  }
}
