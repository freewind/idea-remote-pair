package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyFile}

class IsInPathList(currentProject: MyProject) {
  def apply(file: MyFile, paths: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file.path)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }
}
