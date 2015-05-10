package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class InWatchingList(getServerWatchingFiles: GetServerWatchingFiles, isSubPath: IsSubPath, getRelativePath: GetRelativePath) {

  def apply(file: MyFile): Boolean = {
    getRelativePath(file).exists(path => getServerWatchingFiles().exists(isSubPath(path, _)))
  }

}
