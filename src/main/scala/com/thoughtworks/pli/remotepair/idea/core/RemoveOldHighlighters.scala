package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup._
import com.intellij.openapi.util.Key

class RemoveOldHighlighters {
  def apply(key: Key[Seq[RangeHighlighter]], editor: Editor): Seq[Range] = {
    val oldHLs = Option(editor.getUserData(key)).getOrElse(Nil)
    val oldRanges = oldHLs.map(hl => Range(hl.getStartOffset, hl.getEndOffset))

    oldHLs.foreach(editor.getMarkupModel.removeHighlighter)
    oldRanges
  }
}

