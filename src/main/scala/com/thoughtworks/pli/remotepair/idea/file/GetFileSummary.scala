package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.intellij.remotepair.utils.Md5

class GetFileSummary(getRelativePath: GetRelativePath, getFileContent: GetFileContent, md5: Md5) {
  def apply(file: MyFile): Option[FileSummary] = getRelativePath(file).map(FileSummary(_, md5(file.content.text)))
}
