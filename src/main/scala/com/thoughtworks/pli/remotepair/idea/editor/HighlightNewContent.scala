package com.thoughtworks.pli.remotepair.idea.editor

import java.awt.Color

import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.utils.{Insert, StringDiff}
import com.thoughtworks.pli.remotepair.core.models.MyProject

class HighlightNewContent(currentProject: MyProject) {

  private val changeContentHighlighterKey = new Key[Seq[RangeHighlighter]]("pair-change-content-highlighter")

  def apply(path: String, newContent: String) {
    val attrs = new TextAttributes(Color.GREEN, Color.YELLOW, null, null, 0)
    for {
      editor <- currentProject.getTextEditorsOfPath(path)
      oldRanges = editor.removeOldHighlighters(changeContentHighlighterKey)
      diffs = StringDiff.diffs(editor.document.content, newContent)
      newRanges = diffs.collect {
        case Insert(offset, content) => Range(offset, offset + content.length)
      }
      mergedRanges = mergeRanges(oldRanges, newRanges)
    } editor.newHighlights(changeContentHighlighterKey, attrs, mergedRanges)
  }
  private def mergeRanges(oldRanges: Seq[Range], newRanges: Seq[Range]): Seq[Range] = {
    // FIXME merge them
    newRanges
  }

}
