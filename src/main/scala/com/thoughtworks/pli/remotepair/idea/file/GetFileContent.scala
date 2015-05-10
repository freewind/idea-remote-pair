package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import org.apache.commons.io.IOUtils

class GetFileContent {
  def apply(file: VirtualFile): Content = {
    val charset = file.getCharset.name()
    Content(IOUtils.toString(file.getInputStream, charset), charset)
  }
}
