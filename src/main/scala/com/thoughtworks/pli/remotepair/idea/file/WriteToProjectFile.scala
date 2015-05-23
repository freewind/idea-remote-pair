package com.thoughtworks.pli.remotepair.idea.file

import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import com.thoughtworks.pli.intellij.remotepair.utils.{Delete, Insert, StringDiff}
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

class WriteToProjectFile(currentProject: IdeaProjectImpl) {
  def apply(path: String, content: Content): Unit = {
    val editors = currentProject.getTextEditorsOfPath(path)
    if (editors.nonEmpty) {
      editors.foreach { editor =>
        val document = editor.document
        val currentContent = document.content
        val diffs = StringDiff.diffs(currentContent, content.text)
        diffs.foreach {
          case Insert(offset, newStr) => document.insertString(offset, newStr)
          case Delete(offset, length) => document.deleteString(offset, offset + length)
        }
      }
    } else {
      val file = currentProject.findOrCreateFile(path)
      file.rawFile.setBinaryContent(content.text.getBytes(content.charset))
    }
  }
}
