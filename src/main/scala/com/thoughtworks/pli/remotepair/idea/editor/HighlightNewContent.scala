package com.thoughtworks.pli.remotepair.idea.editor

import java.awt.Color

import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.utils.{Insert, StringDiff}
import com.thoughtworks.pli.remotepair.idea.file.GetDocumentContent
import com.thoughtworks.pli.remotepair.idea.project.GetTextEditorsOfPath

class HighlightNewContent(getTextEditorsOfPath: GetTextEditorsOfPath, newHighlights: NewHighlights, removeOldHighlighters: RemoveOldHighlighters, getDocumentContent: GetDocumentContent) {

  private val changeContentHighlighterKey = new Key[Seq[RangeHighlighter]]("pair-change-content-highlighter")

  def apply(path: String, newContent: String) {
    val attrs = new TextAttributes(Color.GREEN, Color.YELLOW, null, null, 0)
    for {
      editor <- getTextEditorsOfPath(path)
      currentContent = getDocumentContent(editor)
      oldRanges = removeOldHighlighters(changeContentHighlighterKey, editor)
      diffs = StringDiff.diffs(currentContent, newContent)
      newRanges = diffs.collect {
        case Insert(offset, content) => Range(offset, offset + content.length)
      }
      mergedRanges = mergeRanges(oldRanges, newRanges)
    } newHighlights(changeContentHighlighterKey, editor, attrs, mergedRanges)
  }
  private def mergeRanges(oldRanges: Seq[Range], newRanges: Seq[Range]): Seq[Range] = {
    // FIXME merge them
    newRanges
  }

}
