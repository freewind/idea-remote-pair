package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath

class ContainsProjectFile(getProjectBasePath: GetProjectBasePath, isSubPath: IsSubPath) {
  def apply(file: VirtualFile): Boolean = isSubPath(file.getPath, getProjectBasePath())
}
