package com.thoughtworks.pli.remotepair.idea.editor

import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.util.TextRange

class GetSelectionEventInfo {
  def apply(event: SelectionEvent): String = s"${event.getOldRanges.toList.map(textRangeInfo)} => ${event.getNewRanges.toList.map(textRangeInfo)}"
  private def textRangeInfo(textRange: TextRange): String = s"${textRange.getStartOffset} -> ${textRange.getEndOffset} (${textRange.getLength}})"
}
