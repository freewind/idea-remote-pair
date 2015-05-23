package com.thoughtworks.pli.remotepair.idea.editor

import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.core.models.MyProject

class HighlightNewContent(currentProject: MyProject) {

  private val changeContentHighlighterKey = new Key[Seq[RangeHighlighter]]("pair-change-content-highlighter")

  private def mergeRanges(oldRanges: Seq[Range], newRanges: Seq[Range]): Seq[Range] = {
    // FIXME merge them
    newRanges
  }

}
