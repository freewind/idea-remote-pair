package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class IsInPathList(getRelativePath: GetRelativePath) {
  def apply(file: MyFile, paths: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }
}
