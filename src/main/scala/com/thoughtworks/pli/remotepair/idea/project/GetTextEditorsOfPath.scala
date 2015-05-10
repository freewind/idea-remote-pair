package com.thoughtworks.pli.remotepair.idea.project

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor

class GetTextEditorsOfPath(getEditorsOfPath: GetEditorsOfPath) {
  def apply(path: String): Seq[Editor] = {
    getEditorsOfPath(path).collect { case e: TextEditor => e}.map(_.getEditor)
  }
}
