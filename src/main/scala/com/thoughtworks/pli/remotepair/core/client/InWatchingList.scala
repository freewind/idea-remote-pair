package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.core.models.MyFile

class InWatchingList(getServerWatchingFiles: GetServerWatchingFiles, isSubPath: IsSubPath) {

  def apply(file: MyFile): Boolean = {
    file.relativePath.exists(path => getServerWatchingFiles().exists(isSubPath(path, _)))
  }

}
