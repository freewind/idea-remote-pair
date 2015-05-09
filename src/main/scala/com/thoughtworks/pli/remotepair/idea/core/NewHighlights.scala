package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup._
import com.intellij.openapi.util.Key

class NewHighlights {
  def apply(key: Key[Seq[RangeHighlighter]], editor: Editor, attrs: TextAttributes, ranges: Seq[Range]): Unit = {
    val newHLs = ranges.map(r => editor.getMarkupModel.addRangeHighlighter(r.start, r.end,
      HighlighterLayer.LAST + 1, attrs, HighlighterTargetArea.EXACT_RANGE))
    editor.putUserData(key, newHLs)
  }
}
