package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import org.apache.commons.io.IOUtils

class GetFileContent {
  def apply(file: IdeaFileImpl): Content = {
    val charset = file.raw.getCharset.name()
    Content(IOUtils.toString(file.raw.getInputStream, charset), charset)
  }
}
