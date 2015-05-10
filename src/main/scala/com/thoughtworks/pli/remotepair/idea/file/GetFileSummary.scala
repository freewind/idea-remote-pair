package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.intellij.remotepair.utils.Md5

class GetFileSummary(getRelativePath: GetRelativePath, getFileContent: GetFileContent, md5: Md5) {
  def apply(file: VirtualFile): Option[FileSummary] = getRelativePath(file).map(FileSummary(_, md5(getFileContent(file).text)))
}
